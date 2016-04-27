(function() {
  d3.fisheye = {
    scale: function(scaleType) {
      return d3_fisheye_scale(scaleType(), 3, 0);
    },
    scaleFrom : function(scale){
      return d3_fisheye_scale(scale, 3, 0)
    },
    circular: function() {
      var radius = 200,
      minRadiusFactor = .5,
        distortion = 2,
        k0,
        k1,
        focus = [0, 0];

      function fisheye(d, xPosO, yPosO, xPos, yPos) {
        var dx = xPosO - focus[0],
          dy = yPosO - focus[1],
          dd = Math.sqrt(dx * dx + dy * dy);
        if (!dd || dd >= radius) return {x: xPos, y: yPos, z: 1};
        if (!dd || dd <= radius * minRadiusFactor){
          dd = radius * minRadiusFactor;
        }
        var k = k0 * (1 - Math.exp(-dd * k1)) / dd * .75 + .25;
        return {x: focus[0] + dx * k, y: focus[1] + dy * k, z: Math.min(k, 10)};
      }

      function rescale() {
        k0 = Math.exp(distortion);
        k0 = k0 / (k0 - 1) * radius;
        k1 = distortion / radius;
        return fisheye;
      }

      fisheye.radius = function(_) {
        if (!arguments.length) return radius;
        radius = +_;
        return rescale();
      };
      fisheye.minRadiusFactor = function(_) {
        if (!arguments.length) return minRadiusFactor;
        minRadiusFactor = _;
        return rescale();
      };

      fisheye.getRadius = function(_){
        return radius;
      }
      fisheye.getMinRadius = function(){
        return radius * minRadiusFactor;
      }

      fisheye.getFocus = function(_){
        return focus;
      }

      fisheye.distortion = function(_) {
        if (!arguments.length) return distortion;
        distortion = +_;
        return rescale();
      };

      fisheye.focus = function(_) {
        if (!arguments.length) return focus;
        focus = _;
        return fisheye;
      };

      return rescale();
    }
  };


  function d3_fisheye_scale(scale, d, a) {

    function fisheye(_) {
      var x = scale(_),
        left = x < a,
        v,
        range = d3.extent(scale.range()),
        min = range[0],
        max = range[1],
        m = left ? a - min : max - a;
      if (m == 0) m = max - min;
      return (left ? -1 : 1) * m * (d + 1) / (d + (m / Math.abs(x - a))) + a;
    }

    fisheye.distortion = function(_) {
      if (!arguments.length) return d;
      d = +_;
      return fisheye;
    };

    fisheye.focus = function(_) {
      if (!arguments.length) return a;
      a = +_;
      return fisheye;
    };

    fisheye.copy = function() {
      return d3_fisheye_scale(scale.copy(), d, a);
    };

    fisheye.nice = scale.nice;
    fisheye.ticks = scale.ticks;
    fisheye.tickFormat = scale.tickFormat;
    return d3.rebind(fisheye, scale, "domain", "range","getNext");
  }
})();
