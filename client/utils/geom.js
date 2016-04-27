/**
 * Created by conglei on 22/8/14.
 */

geom = (function () {
  var g = {version: "0.0"};

  /**
   * Created by conglei on 22/8/14.
   *
   * Extract the code from d3
   */

  var π = Math.PI, τ = 2 * π, halfπ = π / 2, ε = 1e-6, ε2 = ε * ε, d3_radians = π / 180, d3_degrees = 180 / π;

  function d3_svg_lineMonotoneTangents(points) {
    var tangents = [], d, a, b, s, m = d3_svg_lineFiniteDifferences(points), i = -1, j = points.length - 1;
    while (++i < j) {
      d = d3_svg_lineSlope(points[i], points[i + 1]);
      if (Math.abs(d) < ε) {
        m[i] = m[i + 1] = 0;
      } else {
        a = m[i] / d;
        b = m[i + 1] / d;
        s = a * a + b * b;
        if (s > 9) {
          s = d * 3 / Math.sqrt(s);
          m[i] = s * a;
          m[i + 1] = s * b;
        }
      }
    }
    i = -1;
    while (++i <= j) {
      s = (points[Math.min(j, i + 1)][0] - points[Math.max(0, i - 1)][0]) / (6 * (1 + m[i] * m[i]));
      tangents.push([ s || 0, m[i] * s || 0 ]);
    }
    return tangents;
  }

  g.monotone = function d3_svg_lineMonotone(points) {
    return points.length < 3 ? d3_svg_lineLinear(points) : points[0] + d3_svg_lineHermite(points, d3_svg_lineMonotoneTangents(points));
  };

  g.monotoneSegmented = function svg_linMonotoneSegmented(points) {
    return points.length < 3 ? [d3_svg_lineLinear(points)] : svg_LineHermiteSegment(points,d3_svg_lineMonotoneTangents(points));
  }

  function svg_LineHermiteSegment(points, tangents) {
    var segments = [];
    if (tangents.length < 1 || points.length != tangents.length && points.length != tangents.length + 2) {
      return d3_svg_lineLinear(points);
    }
    var quad = points.length != tangents.length, path = "", p0 = points[0], p = points[1], t0 = tangents[0], t = t0, pi = 1;
    if (quad) {
      segments.push("M" + p0[0] + "," + p0[1] + "Q" + (p[0] - t0[0] * 2 / 3) + "," + (p[1] - t0[1] * 2 / 3) + "," + p[0] + "," + p[1]);
      p0 = points[1];
      pi = 2;
    }
    if (tangents.length > 1) {
      t = tangents[1];
      p = points[pi];
      pi++;
      segments.push("M" + p0[0] + "," + p0[1] + "C" + (p0[0] + t0[0]) + "," + (p0[1] + t0[1]) + "," + (p[0] - t[0]) + "," + (p[1] - t[1]) + "," + p[0] + "," + p[1]);
      for (var i = 2; i < tangents.length; i++, pi++) {
        p = points[pi];
        t = tangents[i];
        t0 = tangents[i-1];
        p0 = points[pi-1];
        segments.push("M" + p0[0] + "," + p0[1] + "C" + (p0[0] + t0[0])  + "," + (p0[1] + t0[1]) + "," + (p[0] - t[0]) + "," + (p[1] - t[1]) + "," + p[0] + "," + p[1]);
      }
    }
    if (quad) {
      var lp = points[pi];
       segments.push("M" + p[0] + "," + p[1] + "Q" + (p[0] + t[0] * 2 / 3) + "," + (p[1] + t[1] * 2 / 3) + "," + lp[0] + "," + lp[1]);
    }
    return segments;
  }


  function d3_svg_lineLinear(points) {
    return "M" + points.join("L");
  }

  function d3_svg_lineFiniteDifferences(points) {
    var i = 0, j = points.length - 1, m = [], p0 = points[0], p1 = points[1], d = m[0] = d3_svg_lineSlope(p0, p1);
    while (++i < j) {
      m[i] = (d + (d = d3_svg_lineSlope(p0 = p1, p1 = points[i + 1]))) / 2;
    }
    m[i] = d;
    return m;
  }

  function d3_svg_lineHermite(points, tangents) {
    if (tangents.length < 1 || points.length != tangents.length && points.length != tangents.length + 2) {
      return d3_svg_lineLinear(points);
    }
    var quad = points.length != tangents.length, path = "", p0 = points[0], p = points[1], t0 = tangents[0], t = t0, pi = 1;
    if (quad) {
      path += "Q" + (p[0] - t0[0] * 2 / 3) + "," + (p[1] - t0[1] * 2 / 3) + "," + p[0] + "," + p[1];
      p0 = points[1];
      pi = 2;
    }
    if (tangents.length > 1) {
      t = tangents[1];
      p = points[pi];
      pi++;
      path += "C" + (p0[0] + t0[0]) + "," + (p0[1] + t0[1]) + "," + (p[0] - t[0]) + "," + (p[1] - t[1]) + "," + p[0] + "," + p[1];
      for (var i = 2; i < tangents.length; i++, pi++) {
        p = points[pi];
        t = tangents[i];
        path += "S" + (p[0] - t[0]) + "," + (p[1] - t[1]) + "," + p[0] + "," + p[1];
      }
    }
    if (quad) {
      var lp = points[pi];
      path += "Q" + (p[0] + t[0] * 2 / 3) + "," + (p[1] + t[1] * 2 / 3) + "," + lp[0] + "," + lp[1];
    }
    return path;
  }

  function d3_svg_lineSlope(p0, p1) {
    return (p1[1] - p0[1]) / (p1[0] - p0[0]);
  }

  //please be careful of the applying orders
  g.transform = {
    value: '',
    begin: function() {
      this.value = '';
      return this;
    },
    end: function() {
      return this.value;
    },
    translate: function(dx, dy) {
      this.value += 'translate(' + dx + ',' + dy + ')';
      return this;
    },
    rotate: function(theta, x0, y0) {
      this.value += 'rotate(' + theta + ',' + x0 + ',' + y0 + ')';
      return this;
    },
    scale: function(fx, fy) {
      this.value += 'scale(' + fx + ',' + fy + ')';
      return this;
    }
  };

  /*
   get a path string by chaining functions
   example:
   g.path.begin() [.move_to(args), ...] .end()
   */
  g.path = {
    value:'',
    x:0,
    y:0,
    s: 0.5, //for curve easing
    begin: function(){
      this.value = '';
      return this;
    },
    move_to: function(x, y) {
      this.value += ' M ' + x + ' ' + y;
      this.x = x;
      this.y = y;
      return this;
    },
    line_to: function(x, y) {
      this.value += ' L ' + x + ' ' + y;
      this.x = x;
      this.y = y;
      return this;
    },
    eased_line_to: function(x, y) {
      var c0x = this.x,
        c0y = this.y,
        c1x = x,
        c1y = y;
      if ((x-this.x) * (y-this.y) > 0) {
        c0y = this.y * (1 - this.s) + y * this.s;
        c1x = this.x * this.s + x * (1 - this.s);
      }
      else {
        c0x = this.x * (1 - this.s) + x * this.s;
        c1y = this.y * this.s + y * (1 - this.s);
      }
      this.bezier_to(c0x, c0y, c1x, c1y, x, y);
      return this;
    },
    h_eased_line_to: function(x, y) {
      this.bezier_to(this.x * (1-this.s) + x * this.s, this.y, this.x * this.s + x * (1-this.s) , y, x, y);
      return this;
    },
    horizontal_to: function (x) {
      this.x = x;
      return this.line_to(x, this.y);
    },
    vertical_to: function(y) {
      this.y = y;
      return this.line_to(this.x, y);
    },
    horizontal_to_relative: function(x) {
      this.value += ' h ' + x;
      this.x = this.x + x;
      return this;
    },
    vertical_to_relative: function(y) {
      this.value += ' v ' + y;
      this.y = this.y + y;
      return this;
    },
    bezier_to: function(cx0, cy0, cx1, cy1, x1, y1) {
      this.x = x1;
      this.y = y1;
      this.value += ' C ' + cx0  + ',' + cy0 + ' ' + cx1 + ', ' + cy1 + ' ' + x1 + ', ' + y1;
      return this;
    },
    close_path: function() {
      this.value += ' Z ';
      return this;
    },
    quad_to: function(cx1,cy1,x1,y1){
      this.x = x1;
      this.y = y1;
      this.value += ' Q ' +  cx1 + ',' + cy1 + ' ' + x1 + ', ' + y1;
      return this;
    },
    end: function() {
      return this.value;
    }
  }

  g.scaling = function(width, height, objects, getter, setter) {

    var x_min = algo.min(objects, function(n) {return getter.call(null, n).x;}),
      x_max = algo.max(objects, function(n) {return getter.call(null, n).x;}),
      y_min = algo.min(objects, function(n) {return getter.call(null, n).y;}),
      y_max = algo.max(objects, function(n) {return getter.call(null, n).y;});

    var s = Math.min(width / (x_max - x_min), height / (y_max - y_min)),
      dx = (width - s * (x_max - x_min)) / 2,
      dy = (height - s * (y_max - y_min)) / 2;

    for (var i = 0, ii = objects.length; i < ii; i++) {
      setter.call(null,
        {x: ((getter.call(null, objects[i]).x - x_min) * s + dx),
          y: ((getter.call(null, objects[i]).y - y_min) * s + dy)},
        objects[i]);
    }
  }

  return g;
})();