<?php
session_start();
?>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="styles.css">
<link rel="stylesheet" type="text/css" href="styles-timecurve.css">
<link href='http://fonts.googleapis.com/css?family=Montserrat:400,700' rel='stylesheet' type='text/css'>
<link href='http://fonts.googleapis.com/css?family=Comfortaa' rel='stylesheet' type='text/css'>

<style>
</style>

</head>
<title>Time Curves</title>
<body>
    <div id="main">
		<div id="top">
		<h1>Time Curves</h1>
			<p>
			Pan and zoom with your mouse. The slider allows you to unfold the timecurve into a regular timeline. A click on the black arrow downloads the curve as SVG file that you can further process in any vector program (e.g. Adobe Illustrator).
			</p>
				<label>Circle Radius: </label>
				<input id="radiusInput" type="text" value="3" onchange="updateRadius()" size="5"></input>

				<label>Line width:
				</label><input id="lineWithInput" type="text" value="1" onchange="updateLineWidth()" size="5"></input>

				<label>Curve opacity:
				</label><input id="opacityInput" type="text" value="1" onchange="updateOpacity()" size="5"></input>

				<input type="checkbox" id="removeNodeOverlap" value="removeNodeOverlap" size="5">Remove Node Overlap<br>
					</td>
		<hr/>
		</div>

		<div id="svgDiv"></div>

		<div id="bottom">
		</div>
	</div>

	<script src="lib/d3.min.js"></script>
    <script src="lib/jquery.js"></script>
    <script src="lib/colors.js"></script>
    <script src="lib/chroma.js"></script>
    <script src="lib/fisheye.js"></script>
   	<script src="curvepile.js"></script>
   	<script src="colors.js"></script>
    <script>

	/////////////////////////////////////////////////////
	// Github: https://github.com/benjbach/timecurves  //
	// Website: www.aviz.fr/~bbach/timecurves          //
	// Contact: benj.bach@gmail.com                    //
	// Contact: shiconglei@gmail.com                   //
	/////////////////////////////////////////////////////


	DATASETS = []

   	var SIZE = 500
   	var TOP = 100
    var MARGIN = 50
    var RADIUS = 3
    var LINE_WIDTH_FACTOR = 1
    var MIN_DIST = RADIUS *2;


   	var curves = []
   	var pileCount = 0
    var piles = []


	pileCount = []
	// for(var i=0 ; i<piles.length; i++){
	// 	piles[i].destroy();
	// }
	piles = []
	var menu = $('#fileSelection')[0]
	var dir = 'example-session'

	d3.json('getCurves.php?dir=' + dir, function(data) {
		console.log(data);
        for(var i=0 ; i<data.length ; i++){
            if(data[i].indexOf('.curve') == -1) continue
            	// console.log('list[i]')
			DATASETS.push(dir + '/'+ data[i])
		}
		console.log(DATASETS)

		DATASETS.forEach(function(d){
			// load(d)
			var cp = new CurvePile('timecurves/' + d, pileCount++, false, 'svgDiv');
			cp.load()
			piles.push(cp)
	    })
    })


	function duplicatePile(id){
		var p
		for(var i=0 ; i<piles.length ; i++){
			// console.log()
			if(piles[i].id == id){
				p = piles[i]
				break
			}
		}

		p = new CurvePile(p.curves, pileCount++)
		p.update();
		piles.push(p)
	}

	/** Shows the upload format defintion*/
	function showFormat(){
		console.log('click')
		$('#dataformat').css('visibility', 'visible')
	}

	function updateRadius(r){
		setRadius(parseFloat(document.getElementById("radiusInput").value));
	}

	function setRadius(r){
		RADIUS = r
		d3.selectAll('.point')
			.attr('r', RADIUS)
	}

	function updateOpacity(r){
		setOpacity(parseFloat(document.getElementById("opacityInput").value));
	}

	var OPACITY = 1
	function setOpacity(o){
		OPACITY = o
		d3.selectAll('.point')
			.style('opacity', OPACITY)
		d3.selectAll('.segment')
			.style('opacity', OPACITY)

		for(var i=0 ; i<piles.length ; i++){
			pile[i].opacity = OPACITY;
		}	
	}


	function setOverlapRemoval(flag){
		for(var i=0 ; i<piles.length ; i++){
			piles[i].update(flag);
		}
	}

	$("#removeNodeOverlap").click(function(){
		setOverlapRemoval(this.checked);

	});

	function updateLineWidth(r){
		setLineWidth(parseFloat(document.getElementById("lineWithInput").value));
	}
	function setLineWidth(w){
		LINE_WIDTH_FACTOR = w

		d3.selectAll('.segment')
 			.style("stroke-width", function(d,i){
 				return LINE_WIDTH_FACTOR;
		    })
		
	}


    function setWindowSize(){
    	var newSize = parseInt(document.getElementById("windowSizeInput").value);
    	MARGIN = MARGIN * newSize/SIZE
    	SIZE = newSize;
		for(var i=0 ; i<piles.length ; i++){
   			piles[i].updateSize(SIZE)
   		}
    }

	</script>
	<?php
session_destroy();
?>
</body>
</html>
