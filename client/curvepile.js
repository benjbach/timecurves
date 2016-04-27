function CurvePile(file, id, isGallery, elementId) {

    // width of slider to turn curve into timeline
    var SLIDER_WIDTH = SIZE/3

    // margins
    TOP = 40;
    GAP = 40;
    this.file = file;

    that = this;

    // curve visual parameters
    this.opacity = 1;
    this.linewidth = 2

    // for website only 
    this.isGallery = isGallery;

    if (!isGallery)
        this.isGallery = false


    this.id = id
    this.elementId = elementId
    // remove overlap of points
    this.removeOverlap = false;
    if (isGallery)
        this.removeOverlap = true;

    // DATA
    this.curves = []
    this.strengthenCurve = false;
    this.strengthenCurveLevel = 0;

    // VISUAL
    // function for mapping horizontal MDS point coordinats to screen x corrdinates
    this.x = d3.scale.linear().range([MARGIN, SIZE - MARGIN])
    // function for mapping vertical MDS point coordinats to screen y corrdinates
    this.y = d3.scale.linear().range([TOP + MARGIN, SIZE - MARGIN])
    // function mapping time stamps to screen x positions, for drawing them on timeline.
    this.timeScale = d3.scale.linear().range([MARGIN, SIZE - MARGIN])

    d3.select('#svg_' + id).remove()
    // create main div
    this.div = d3.select('#'+this.elementId)
        .append('div')
        .attr('style', 'float:left; width:' + SIZE + 'px;')
        .attr('class', 'tileDiv')

    // add border, if curve is shown on website
    if (!isGallery){
        this.div.attr('class', 'tileDiv fineBorder')
    }

    // create svg element
    this.svg = this.div
        .append('svg')
        .attr('id', 'svg_' + id)
        .attr('width', SIZE)
        .attr('height', SIZE)
        .attr('xlink', 'http://www.w3.org/1999/xlink')

    this.p = this.div.append('p')
        .attr('class', 'commentsBox')
        .attr('width', SIZE)


    this.points = [] // time points in curve
    this.paths = [] // paths between consequtive timepoints


    this.g = this.svg.append('g')
    this.bkg = this.g.append('rect')
        .attr('width', SIZE)
        .attr('height', SIZE)
        .attr('fill', '#fff')

    this.gBackground = this.g.append('g')
    this.gCurve = this.g.append('g')
    this.gPoints = this.g.append('g')
    this.gForeground = this.g.append('g')

    var LEGEND_LEFT = 30
    var LEGEND_TOP = 50

    // load data
    CurvePile.prototype.load = function(d) {
        var _this = this
        
        // d3 routing to load .curve file (json format)
        d3.json(file, function(data) {

            _this.data = data
            // array containing all curves in data set
            _this.curves = []

            // Sort nodes
            var nodes = data.nodes

            // GET MIN/MAX EXTENSION of MDS coordinates FOR CENTRIC PLACEMENT of curve
            var xMax = -10000
            var xMin = 10000
            var yMax = -10000
            var yMin = 10000

            var cName, c, i
            nodes.forEach(function(n) {
                xMax = Math.max(n.x, xMax)
                xMin = Math.min(n.x, xMin)
                yMax = Math.max(n.y, yMax)
                yMin = Math.min(n.y, yMin)

                // init visual attributes
                n.xOffset = 0
                n.yOffset = 0

                cName = n.n.split('#')[1] //
                if (!cName) {
                    cName = _this.file.split('/')[1].split('-t')[0]
                }
                cName = cName.replace('$', ': ').replace(/_/g, ' ')

                i = getCurve(_this.curves, cName)
                if (i == -1) {
                    c = new Object();
                    c.nodes = []
                    c.name = cName
                    _this.curves.push(c)
                    i = _this.curves.length - 1
                }
                _this.curves[i].nodes.push(n)

            })
            // set min/max to x and y mapping function (see above)
            _this.x.domain([xMin, xMax])
            _this.y.domain([yMin, yMax])

            var gMinTime = Date.now();
            var gMaxTime = 0;

            // sort points in each curve according to their time step.
            var n, curve
            for (var i = 0; i < _this.curves.length; i++) {
                curve = _this.curves[i]

                curve.nodes.sort(function(n1, n2) {
                    return n1.t - n2.t;
                })

                var minTime = Date.now()
                var maxTime = 0
                for (var j = 0; j < curve.nodes.length; j++) {
                    n = curve.nodes[j]
                    n.t = parseInt(n.t)
                    minTime = Math.min(n.t, minTime)
                    maxTime = Math.max(n.t, maxTime)
                }
                curve.maxTime = maxTime
                curve.minTime = minTime
                gMinTime = Math.min(curve.minTime, gMinTime);
                gMaxTime = Math.max(curve.maxTime, gMaxTime);
                curve.visible = true
                curve.pointColor = chroma.scale(colorScales[i % colorScales.length]).mode('lab')
                curve.pointColor.domain([minTime, maxTime])
            }
            _this.timeScale.domain([gMinTime, gMaxTime]);

            _this.draw();
            _this.svg.call(d3.behavior.zoom().on("zoom", function(d) {
                _this.zoom(_this.g)
            }))

        })
    }

    // d3 zoom handler
    CurvePile.prototype.zoom = function(d) {
        d.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    }


    // draw curves
    CurvePile.prototype.draw = function() {
        var d;
        // for every curve...
        for (var i = 0; i < this.curves.length; i++) {
            // for every time points (node) in curve ...
            for (var j = 0; j < this.curves[i].nodes.length; j++) {
                this.curves[i].nodes[j].mdsX = this.curves[i].nodes[j].x;
                this.curves[i].nodes[j].mdsY = this.curves[i].nodes[j].y;
                // save original coordinates to enable set back after use of fisheye lens distortion.
                this.curves[i].nodes[j].mdsXO = this.curves[i].nodes[j].mdsX;
                this.curves[i].nodes[j].mdsYO = this.curves[i].nodes[j].mdsY;
                this.curves[i].nodes[j].timePoint = this.curves[i].nodes[j].t;
                this.curves[i].nodes[j].identicalPoints = []
                d = new Date(parseInt(this.curves[i].nodes[j].t))
                // format time label
                this.curves[i].nodes[j].timeLabel = d.getFullYear() + '/' + (d.getMonth() + 1) + '/' + d.getDate() + '-' + d.getHours() + ':' + d.getMinutes() + ':' + d.getSeconds() + '.' + d.getMilliseconds();

                // find overlapping time points
                var list = []
                var n
                for (var k = j - 1; k >= 0; k--) {
                    if ((this.curves[i].nodes[k].x == this.curves[i].nodes[j].x) && (this.curves[i].nodes[k].y == this.curves[i].nodes[j].y)) {
                        n = this.curves[i].nodes[k]
                        list.push(this.curves[i].nodes[k])
                    }
                }
                if (list.length > 0) {
                    list.push(this.curves[i].nodes[j])
                    n.identicalPoints = list
                }
            }

            // remove overlaps if requested
            if (this.removeOverlap) {
                this.removeOverlaps(i)
            }

            // Drawing order: 
            this.drawCurvePath(i) // curve line
            this.drawClusterPoints(i) // gray halos for clusters
            this.drawStartEnd(i) // glyphs to indicate start and end of curve
            this.drawPoints(i) // time points
            this.addFisheye(); // interactive fisheye lens distortion

            // if data file contains comments, show them (for website purposes only)
            if (this.data.comments) {
                this.p.html(this.data.comments.replace(/"/g, ''))
            }
        }
        // draw name of curve, if provided by data
        this.drawCurveLabel()
    }


    // udpate/redraw curve after user interactions
    CurvePile.prototype.update = function(removeOverlap) {

        this.gBackground.selectAll("*").remove();
        this.gCurve.selectAll("*").remove();
        this.gPoints.selectAll("*").remove();

        if (arguments.length)
            this.removeOverlap = removeOverlap;

        for (var i = 0; i < this.curves.length; i++) {
            for (var j = 0; j < this.curves[i].nodes.length; j++) {
                this.curves[i].nodes[j].mdsX = this.curves[i].nodes[j].mdsXO;
                this.curves[i].nodes[j].mdsY = this.curves[i].nodes[j].mdsYO;
            }
            if (this.removeOverlap) {
                this.removeOverlaps(i)
            }
            this.drawClusterPoints(i)
            this.drawCurvePath(i)
            this.drawPoints(i)
            this.drawCurveLabel(i)
        }
    }

    // interpolate between curve and timeline with parameter frac \in [0,1]
    CurvePile.prototype.strengthCurve = function(frac) {
    
        for (var i = 0; i < this.curves.length; i++) {
            var fy = SIZE - GAP * (i + 1);
            var fy_invert = this.y.invert(fy);
            for (var j = 0; j < this.curves[i].nodes.length; j++) {
                var fx = this.timeScale(this.curves[i].nodes[j].t);
                var fx_invert = this.x.invert(fx);
                this.curves[i].nodes[j].mdsX = this.curves[i].nodes[j].mdsXO * (1 - frac) + fx_invert * (frac);
                this.curves[i].nodes[j].mdsY = this.curves[i].nodes[j].mdsYO * (1 - frac) + fy_invert * (frac);
            }
            // this.ClusterPoints(i);
            this.updateClusterPoints(i)
            this.updateCurvePath(i);
            this.updatePoints(i);
            this.updateStartEnd(i)
        }

    };

    // update curve path for curve i
    CurvePile.prototype.updateCurvePath = function(i) {
        var index = i;
        var _this = this;
        var curve = this.curves[i];

        // set-up basis spline
        var curveFunction = d3.svg.line()
            .x(function(d) {
                return _this.x(d[0]);
            })
            .y(function(d) {
                return _this.y(d[1]);
            })
            .interpolate("basis");

        // caluclate points for each curve segment
        this.calPath(curve);

        var nodes = curve.nodes;

        // draw curve segments (one segment for any consecutive point pair)
        var p = this.gCurve.selectAll(".segment.curve_" + index)
            .transition()
            .duration(10)
            .attr("d", function(d) {
                return curveFunction(d.path);
            })


        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].fisheye) {
                nodes[i].mdsX = nodes[i].xBackup;
                nodes[i].mdsY = nodes[i].yBackup;
            }
        }
    };

    // 
    CurvePile.prototype.updatePoints = function(i) {
        var _this = this
        var curve = this.curves[i]
        var nodes = curve.nodes;
      
        // Draw actual points
        var points = this.gPoints.selectAll('.point_' + i)
            .transition()
            .duration(10)
            .attr('cx', function(d, i) {
                return _this.x(d.mdsX)
            })
            .attr('cy', function(d, i) {
                return _this.y(d.mdsY)
            })
    };

    CurvePile.prototype.updateStartEnd = function(i) {
        var curve = this.curves[i]
        var nodes = curve.nodes;
        var _this = this
        var startPoint = this.gBackground.selectAll('.startmark')
            .transition()
            .duration(10)
            .attr('points', function(d) {
                return (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) - 10) + " " + (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) + 10) + " " + (_this.x(d.mdsX)) + "," + (_this.y(d.mdsY));
            });


        var endPoint = this.gBackground.selectAll('.endmark')
            .transition()
            .duration(10)
            .attr('x', function(d) {
                return _this.x(d.mdsX) - 10;
            })
            .attr('y', function(d) {
                return _this.y(d.mdsY) - 10;
            })
    }

    CurvePile.prototype.drawStartEnd = function(i) {
        var curve = this.curves[i]
        var nodes = curve.nodes;
        var _this = this
        var startPoint = this.gBackground.selectAll('startPoint')
            .data([nodes[0]])
            .enter()
            .append('polygon')
            .attr('points', function(d) {
                return (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) - 10) + " " + (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) + 10) + " " + (_this.x(d.mdsX)) + "," + (_this.y(d.mdsY));
            })
            .classed("startmark", true)
            .classed("mark_" + i, true)
            .style('fill', '#000')
            .style('opacity', .1)


        var endPoint = this.gBackground.selectAll('endPoint')
            .data([nodes[nodes.length - 1]])
            .enter()
            .append('rect')
            .attr('x', function(d) {
                return _this.x(d.mdsX) - 10;
            })
            .attr('y', function(d) {
                return _this.y(d.mdsY) - 10;
            })
            .attr('width', 20)
            .attr('height', 20)
            .classed("endmark", true)
            .classed("mark_" + i, true)
            .style('fill', '#000')
            .style('opacity', .1)
    }


    CurvePile.prototype.drawCurveLabel = function() {
        var curve = this.curves[0];
        var name = curve.name.split('.')[0]
        var _this = this
        name = name.charAt(0).toUpperCase() + name.slice(1);

        this.div.append('text')
            .attr('x', 0)
            .attr('class', 'curveName')
            .text(name + ' (' + curve.nodes.length + ')')

        // Export button
        this.div.append('img')
            .attr('height', 20)
            .attr('width', 20)
            .attr('y', 0)
            .attr('src', 'material/ico_export.svg')
            .style('margin-left', '8px')
            .on('click', function() {
                _this.exportSVG()
            })
            .on('mouseover', function(){
                _this.svg.append('text')
                    .attr('class','downloadLabel')
                    .attr('x',SIZE -10)
                    .attr('y',SIZE -15)
                    .attr('text-anchor', 'end')
                    .text('Download svg')
            })
            .on('mouseout', function(){
                _this.svg.selectAll('.downloadLabel').remove()
            })


        // if (!this.isGallery) {

            var drag = d3.behavior.drag()
                .origin(Object)
                .on('dragstart', function() {
                    d3.event.sourceEvent.stopPropagation();
                })
                .on("drag", dragMove)
                .on('dragend', dragEnd);

            function dragMove(d) {
                d3.select(this)
                    .attr("cx", d.x = Math.max(0, Math.min(SLIDER_WIDTH, d3.event.x)))
                    .attr("cy", d.y = 1);
                _this.strengthCurve(d.x / SLIDER_WIDTH);
            }

            function dragEnd() {
            }

            var slider = this.svg.append('g')
                .attr('transform', 'translate( '+SLIDER_WIDTH+ ', '+ (SIZE-7) +')');

            slider.append('rect')
                .attr("height", 1)
                .attr("width", SLIDER_WIDTH)
                .attr('fill', '#C0C0C0');

            slider.selectAll('dcircle')
                .data([{
                    x: 0,
                    y: 1
                }])
                .enter()
                .append("circle")
                .attr("r", 5)
                .attr("cx", function(d) {
                    return d.x;
                })
                .attr("cy", function(d) {
                    return d.y;
                })
                .style("fill", "white")
                .style("stroke", "black")
                .call(drag)
                .on('mouseover', function(d){
                    _this.svg.append('text')
                        .attr('class','sliderLabel')
                        .attr('x',SLIDER_WIDTH)
                        .attr('y',SIZE-14)
                        // .text('Drag me to unfold')
                        .text('')
                })
                .on('mouseout', function(){
                    _this.svg.selectAll('.sliderLabel').remove()
                })

    }

    CurvePile.prototype.addFisheye = function(x, y) {
        var _this = this;
        this.svg.on("mousemove", function() {
            d3.selectAll(".fisheye").remove();

            if (!d3.event.shiftKey) return;

            var fisheye = d3.fisheye.circular()
                .radius(70)
                .minRadiusFactor(.3)
                .distortion(20);

            fisheye.focus(d3.mouse(this));

            // OUTER FISHEYE RING
            _this.gPoints.append("circle")
                .attr("cx", function(d) {
                    return fisheye.getFocus()[0];
                })
                .attr("cy", function(d) {
                    return fisheye.getFocus()[1];
                })
                .attr("class", "fisheye")
                .attr("r", function(d) {
                    var radius = fisheye.getRadius(d);
                    return radius;
                })
                .attr("stroke-width", 1)
                .attr("stroke", "grey")
                .style('fill', 'none')


            _this.gPoints.selectAll(".point")
                .each(function(d) {
                    d.fisheye = fisheye(d, _this.x(d.mdsXO), _this.y(d.mdsYO), _this.x(d.mdsX), _this.y(d.mdsY));
                })
                .attr("cx", function(d) {
                    return d.fisheye.x;
                })
                .attr("cy", function(d) {
                    return d.fisheye.y
                })
                .attr("z", "1000")

            for (var i = 0; i < _this.curves.length; i++) {
                _this.drawCurvePath(i);
            }

            _this.gBackground.selectAll('.startmark')
                .attr('points', function(d) {
                    return (d.fisheye.x - 30) + "," + (d.fisheye.y - 10) + " " + (d.fisheye.x - 30) + "," + (d.fisheye.y + 10) + " " + (d.fisheye.x) + "," + (d.fisheye.y);
                });

            _this.gBackground.selectAll('.endmark')
                .attr('x', function(d) {
                    return d.fisheye.x - 10;
                })
                .attr('y', function(d) {
                    return d.fisheye.y - 10;
                })
                .attr('width', 20)
                .attr('height', 20)
                .style('fill', "lightgrey");

        });


        this.g.on("mouseout", function() {
            d3.selectAll(".fisheye").remove();
            for (var i = 0; i < _this.curves.length; i++) {
                _this.drawCurvePath(i);
            }

            _this.gBackground.selectAll(".clusterPoint")
                .attr("cx", function(d) {
                    delete d.fisheye;
                    return _this.x(d.mdsX);
                })
                .attr("cy", function(d) {
                    return _this.y(d.mdsY);
                })
                .attr("z", "1000")

            _this.gBackground.selectAll(".identityPoint")
                .attr("cx", function(d) {
                    // delete d[0].fisheye;
                    return _this.x(d.mdsX);
                })
                .attr("cy", function(d) {
                    return _this.y(d.mdsY);
                })
                .attr("z", "1000")

            _this.gPoints.selectAll(".point")
                .attr("cx", function(d) {
                    delete d.fisheye;
                    return _this.x(d.mdsX);
                })
                .attr("cy", function(d) {
                    return _this.y(d.mdsY);
                })
                .attr("z", "1000")


            _this.gBackground.selectAll('.startmark')
                .attr('points', function(d) {
                    return (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) - 10) + " " + (_this.x(d.mdsX) - 30) + "," + (_this.y(d.mdsY) + 10) + " " + (_this.x(d.mdsX)) + "," + (_this.y(d.mdsY));
                })

            _this.gBackground.selectAll('.endmark')
                .attr('x', function(d) {
                    return _this.x(d.mdsX) - 10;
                })
                .attr('y', function(d) {
                    return _this.y(d.mdsY) - 10;
                })
                .attr('width', 20)
                .attr('height', 20)
                .style('fill', "lightgrey");

        });
    }


    CurvePile.prototype.calPath = function(curve) {

        var _this = this;
        var nodes = curve.nodes;
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].fisheye) {
                nodes[i].xBackup = nodes[i].mdsX;
                nodes[i].yBackup = nodes[i].mdsY;
                nodes[i].mdsX = _this.x.invert(nodes[i].fisheye.x);
                nodes[i].mdsY = _this.y.invert(nodes[i].fisheye.y);
            }
        }
        var cPrev = []
        var cSucc = []
        var c1, c2;
        var p2, p2, p;
        var v13, v12, v23;
        var l13, l12;
        var v, l, v1, v2

        var SMOOTH = .3; // smoothing the curve
        var a
        var m = 1;
        cSucc.push([nodes[0].mdsX, nodes[0].mdsY])
        // for every node pair, calculate helper points to draw curve. (see paper for more information)
        for (var i = 1; i < nodes.length - 1; i++) {
            p2 = nodes[i];
            l12 = 0
            l23 = 0
            m = 1;

            p1 = nodes[i - m];
            v12 = [p2.mdsX - p1.mdsX, p2.mdsY - p1.mdsY]
            l12 = Math.sqrt(v12[0] * v12[0] + v12[1] * v12[1])
            p3 = nodes[i + m];
            v23 = [p3.mdsX - p2.mdsX, p3.mdsY - p2.mdsY]
            l23 = Math.sqrt(v23[0] * v23[0] + v23[1] * v23[1])

            c1 = null
            c2 = null
            if (l12 == 0)
                c1 = [p2.mdsX, p2.mdsY]
            if (l23 == 0)
                c2 = [p2.mdsX, p2.mdsY]

            if (l12 > 0 || l23 > 0) {
                while (l12 == 0 && (i - m) > 0) {
                    m += 1;
                    p1 = nodes[i - m];
                    v12 = [p2.mdsX - p1.mdsX, p2.mdsY - p1.mdsY]
                    l12 = Math.sqrt(v12[0] * v12[0] + v12[1] * v12[1])
                }

                m = 1
                while (l23 == 0 && (i + m) < nodes.length - 1) {
                    m += 1;
                    p3 = nodes[i + m];
                    v23 = [p3.mdsX - p2.mdsX, p3.mdsY - p2.mdsY]
                    l23 = Math.sqrt(v23[0] * v23[0] + v23[1] * v23[1])
                }

                v13 = [p3.mdsX - p1.mdsX, p3.mdsY - p1.mdsY]
                l13 = Math.sqrt(v13[0] * v13[0] + v13[1] * v13[1])

                v = [v13[0], v13[1]];
                if (l13 == 0) {
                    v[0] = v12[1] * Math.random()
                    v[1] = -v12[0] * Math.random()
                }
                l = Math.sqrt(v[0] * v[0] + v[1] * v[1])

                v1 = [0, 0]
                v1[0] = v[0] / l
                v1[1] = v[1] / l
                v1[0] *= l12 * SMOOTH;
                v1[1] *= l12 * SMOOTH;
              
                v2 = [0, 0]
                v2[0] = v[0] / l
                v2[1] = v[1] / l
                v2[0] *= l23 * SMOOTH;
                v2[1] *= l23 * SMOOTH;
                if (!c1)
                    c1 = [p2.mdsX - v1[0], p2.mdsY - v1[1]]
                if (!c2)
                    c2 = [p2.mdsX + v2[0], p2.mdsY + v2[1]]
            } else {
                // console.log('no segment')
            }

            cPrev.push(c1)
            cSucc.push(c2)
        }

        // add last node, since last node does not has segment.
        cPrev.push([nodes[nodes.length - 1].mdsX, nodes[nodes.length - 1].mdsY])

        // calcualate time elapsed between time points to define curve thickness.
        var TD_MAX = -100000; // time difference min
        var TD_MIN = 999999999999; // time difference min

        var p;
        for (var i = 0; i < nodes.length - 1; i++) {
            p1 = nodes[i];
            p2 = nodes[i + 1];
            p1.path = [
                [p1.mdsX, p1.mdsY],
                [cSucc[i][0], cSucc[i][1]],
                [cPrev[i][0], cPrev[i][1]],
                [p2.mdsX, p2.mdsY]
            ];
            p1.nextTimepoint = p2.timePoint;
            TD_MAX = Math.max(TD_MAX, p2.timePoint - p1.timePoint);
            if (p2.timePoint - p1.timePoint > 0)
                TD_MIN = Math.min(TD_MIN, p2.timePoint - p1.timePoint);
        }

        _this.strokeWidthScale = d3.scale.linear().domain([TD_MIN, TD_MAX]).range([1, 3])

    }


    // draw curve path based on points calulated in calPath(i)
    CurvePile.prototype.drawCurvePath = function(i) {

        var index = i;
        var _this = this;

        var curveFunction = d3.svg.line()
            .x(function(d) {
                return _this.x(d[0]);
            })
            .y(function(d) {
                return _this.y(d[1]);
            })
            .interpolate("basis");

        var curve = this.curves[i];

        this.calPath(curve);

        var nodes = curve.nodes;

        this.gCurve.selectAll(".segment.curve_" + index).remove();
        var p = this.gCurve.selectAll(".segment.curve_" + index)
            .data(nodes)
            .enter()
            .insert("path", ":first-child")
            .filter(function(d) {
                return d.path;
            })
            .attr("class", "segment curve_" + index)
            .attr("style", function(d, i) {
                return "stroke: " + curve.pointColor(d.timePoint).hex() + "; fill: none;";
            })
            .style("stroke-width", function(d, i) {
                var td = d.nextTimepoint - d.timePoint;
                console.log()
                return _this.strokeWidthScale(td)* LINE_WIDTH_FACTOR;
            })
            .style("opacity", this.opacity)
            .attr("d", function(d) {
                return curveFunction(d.path);
            })


        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].fisheye) {
                nodes[i].mdsX = nodes[i].xBackup;
                nodes[i].mdsY = nodes[i].yBackup;
            }
        }
    }

    // remove overlaps between points
    CurvePile.prototype.removeOverlaps = function(i) {
        var curve = this.curves[i]
        var nodes = curve.nodes
        var dx, dy, dd, xx, yy;
        var overlaps = 1;
        var l;
        var count = 0;
        var dm;
        var MIN_DIST = RADIUS*2; // minimal distance between points
 
        for (var i = 0; i < nodes.length - 1; i++)
            nodes[i].displaced = 0;

        var n1, n2;
        while (overlaps > 0 && count < 400) {
            overlaps = 0;
            for (var i = 0; i < nodes.length - 1; i++) {
                for (var j = i + 1; j < nodes.length; j++) {
                    var dx = this.x(nodes[i].mdsX) - this.x(nodes[j].mdsX);
                    var dy = this.y(nodes[i].mdsY) - this.y(nodes[j].mdsY);
                    var dd = Math.sqrt(dx * dx + dy * dy);
                    if (dd < MIN_DIST && dd > 1) {
                        overlaps++;
                        // remove overlap
                        l = (MIN_DIST - dd) / 4;
                        xx = l * (dx / dd);
                        yy = l * (dy / dd);
                        nodes[i].mdsX = this.x.invert(this.x(nodes[i].mdsX) + xx);
                        nodes[i].mdsY = this.y.invert(this.y(nodes[i].mdsY) + yy);
                        nodes[j].mdsX = this.x.invert(this.x(nodes[j].mdsX) - xx);
                        nodes[j].mdsY = this.y.invert(this.y(nodes[j].mdsY) - yy);
                        nodes[i].displaced = 1;
                        nodes[j].displaced = 1;
                    }
                }
            }
            count++;
        }
    }

    // update location of cluster points after fisheye use
    CurvePile.prototype.updateClusterPoints = function(i) {
        var curve = this.curves[i]
        var nodes = curve.nodes;
        var _this = this

        // Draw revision points
        this.gBackground.selectAll(".identityPoint").remove()
        var revisionPoint = this.gBackground.selectAll(".identityPoint")
            .transition()
            .duration(10)
            .attr("cx", function(d) {
                return _this.x(d.mdsX)
            })
            .attr("cy", function(d) {
                return _this.y(d.mdsY)
            })

        // Draw cluster points
        this.gBackground.selectAll(".clusterPoint_" + this.id)
        if (this.removeOverlap) {
            var clusterPoint = this.gBackground.selectAll(".clusterPoint")
                .transition()
                .duration(10)
                .attr("cx", function(d) {
                    return _this.x(d.mdsX)
                })
                .attr("cy", function(d) {
                    return _this.y(d.mdsY)
                })
        }
    }

    CurvePile.prototype.drawClusterPoints = function(i) {
        var curve = this.curves[i]
        var nodes = curve.nodes;
        var _this = this

        // Draw revision points
        // identity points are those with blue halos.
        this.gBackground.selectAll(".identityPoint").remove()
        var revisionPoint = this.gBackground.selectAll(".identityPoint")
            .data(nodes.filter(function(d) {
                return d.identicalPoints.length > 0
            }))
            .enter()
            .append("circle")
            .attr("r", function(d) {
                return RADIUS + 10;
            })
            .style("fill", "#03f")
            .style("opacity", .2)
            .attr("class", "identityPoint")
            .attr("cx", function(d) {
                return _this.x(d.mdsX)
            })
            .attr("cy", function(d) {
                return _this.y(d.mdsY)
            })

        // Draw cluster points (gray halos for very similar, but not identical points)
        this.gBackground.selectAll(".clusterPoint_" + this.id)
        if (this.removeOverlap) {
            var clusterPoint = this.gBackground.selectAll(".clusterPoint_" + this.id)
                .data(nodes.filter(function(d, i) {
                    return d.displaced == 1
                })).enter()
                .append("circle")
                .attr("r", function(d) {
                    return RADIUS + 10;
                })
                .style('fill', '#eee')
                .attr("class", "clusterPoint")
                .attr("cx", function(d) {
                    return _this.x(d.mdsX)
                })
                .attr("cy", function(d) {
                    return _this.y(d.mdsY)
                })
        }
    }


    CurvePile.prototype.drawPoints = function(i) {
        var _this = this
        var curve = this.curves[i]
        var nodes = curve.nodes;
        // Draw actual points
        var points = this.gPoints.selectAll('.point point_' + i)
            .data(nodes)
            .enter()
            .append('circle')
            .attr('class', 'point point_' + i)
            .attr('cx', function(d, i) {
                return _this.x(d.mdsX)
            })
            .attr('cy', function(d, i) {
                return _this.y(d.mdsY)
            })
            .attr('r', RADIUS)
            .style('fill', function(d, i) {
                return curve.pointColor(d.timePoint)
            })
            .style('stroke', '#fff')
            .classed('displaced', function(d) {
                return d.displaced;
            })
            .on('mouseover', function(d, i) {
                _this.showLabel(this, d, i, true)
            })
            .on('mouseout', function(d, i) {
                _this.showLabel(this, d, i, false)
            })
    }

    CurvePile.prototype.showLabel = function(element, d, i, b) {
        var _this = this
        if (b) {
            this.gForeground.append('text')
                .text(d.timeLabel.split('-')[0])
                .attr('id', 'label1')
                .attr('x', parseInt(d3.select(element).attr('cx')) + 10)
                .attr('y', d3.select(element).attr('cy'))
                .attr('class', 'label')
            this.gForeground.append('text')
                .text(d.timeLabel.split('-')[1])
                .attr('id', 'label2')
                .attr('x', parseInt(d3.select(element).attr('cx')) + 10)
                .attr('y', parseInt(d3.select(element).attr('cy')) + 15)
                .attr('class', 'label')
        } else {
            d3.select('#label1').remove()
            d3.select('#label2').remove()
        }

    }

    // Set curve visual parameters
    CurvePile.prototype.setVisibility = function(i, b) {
        var _this = this

        var button = this.g.select('#button_' + i)
        var c = this.curves[i]
        if (!b) {
            c.visible = false
            this.g.select('#button_' + i).style('fill', '#fff')
            this.g.selectAll('.curve_' + i).style('opacity', 0)
            this.g.selectAll('.point_' + i).style('opacity', 0)
            this.g.selectAll('.mark_' + i).style('opacity', 0);
        } else {
            c.visible = true
            this.g.select('#button_' + i).style('fill', c.pointColor(c.minTime + (c.maxTime - c.minTime) / 2))
            this.g.selectAll('.curve_' + i).style('opacity', this.opacity)
            this.g.selectAll('.point_' + i).style('opacity', this.opacity)
            this.g.selectAll('.mark_' + i).style('opacity', this.opacity);
        }
    }

    // destroy all graphics
    CurvePile.prototype.destroy = function() {
        this.g.selectAll("*").remove()
        d3.select('#svg_' + id).remove()
    }


    // update curve if window size changes
    CurvePile.prototype.updateSize = function(size) {
        this.x = this.x.range([MARGIN, size - MARGIN])
        this.y = this.y.range([MARGIN, size - MARGIN])

        this.div.attr('style', 'float:left; width:' + SIZE + 'px;')

        this.svg.attr('width', size)
        this.svg.attr('height', size)

        this.g.attr('width', size)
        this.g.attr('height', size)
        this.update(this.removeOverlap)
    }

    // export curve as svg.
    CurvePile.prototype.exportSVG = function() {
        // console.log('export', d.graphfile)
        var dump = '<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/ SVG/1.1/DTD/svg11.dtd">'
        dump += '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">'

        dump += '<style type="text/css" ><![CDATA['
        dump += $('style').html()
        dump += ']]></style>'

        dump += this.g.html()
        dump += '</svg>'

        var outfile = this.curves[0].name.split('.')[0]
        console.log(outfile)
        var textFileAsBlob = new Blob([dump], {
            type: 'text/text'
        });
        var fileNameToSaveAs = outfile + '.svg'
        var downloadLink = document.createElement("a")
        downloadLink.download = fileNameToSaveAs;
        downloadLink.href = window.webkitURL.createObjectURL(textFileAsBlob);
        downloadLink.click();
    }

    // return the curve with a specific name (user interaction)
    function getCurve(curves, name) {
        for (var i = 0; i < curves.length; i++) {
            if (curves[i].name.indexOf(name) > -1)
                return i
        }
        return -1
    }



}
