/////////////////////////////////////////////////////
// Github: https://github.com/benjbach/timecurves  //
// Website: www.aviz.fr/~bbach/timecurves          //
// Contact: benj.bach@gmail.com                    //
// Contact: shiconglei@gmail.com                   //
////////////////////////////////////////////////////


Date.prototype.customFormat = function (formatString) {
  var YYYY, YY, MMMM, MMM, MM, M, DDDD, DDD, DD, D, hhh, hh, h, mm, m, ss, s, ampm, AMPM, dMod, th;
  var dateObject = this;
  YY = ((YYYY = dateObject.getFullYear()) + "").slice(-2);
  MM = (M = dateObject.getMonth() + 1) < 10 ? ('0' + M) : M;
  MMM = (MMMM = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"][M - 1]).substring(0, 3);
  DD = (D = dateObject.getDate()) < 10 ? ('0' + D) : D;
  DDD = (DDDD = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][dateObject.getDay()]).substring(0, 3);
  th = (D >= 10 && D <= 20) ? 'th' : ((dMod = D % 10) == 1) ? 'st' : (dMod == 2) ? 'nd' : (dMod == 3) ? 'rd' : 'th';
  formatString = formatString.replace("#YYYY#", YYYY).replace("#YY#", YY).replace("#MMMM#", MMMM).replace("#MMM#", MMM).replace("#MM#", MM).replace("#M#", M).replace("#DDDD#", DDDD).replace("#DDD#", DDD).replace("#DD#", DD).replace("#D#", D).replace("#th#", th);

  h = (hhh = dateObject.getHours());
  if (h == 0) h = 24;
  if (h > 12) h -= 12;
  hh = h < 10 ? ('0' + h) : h;
  AMPM = (ampm = hhh < 12 ? 'am' : 'pm').toUpperCase();
  mm = (m = dateObject.getMinutes()) < 10 ? ('0' + m) : m;
  ss = (s = dateObject.getSeconds()) < 10 ? ('0' + s) : s;
  return formatString.replace("#hhh#", hhh).replace("#hh#", hh).replace("#h#", h).replace("#mm#", mm).replace("#m#", m).replace("#ss#", ss).replace("#s#", s).replace("#ampm#", ampm).replace("#AMPM#", AMPM);
}

String.prototype.formatDate = function (formatString) {

  var sec_num = parseInt(this, 10); // don't forget the second param
//    console.log(sec_num)
  var now = new Date(sec_num);


  return now.customFormat(formatString);
}

/**
 * this namespace is for all the constant values.
 * @namespace
 * */
progresio.utils.const = progresio.utils.const || {};


/**
 * type of ordering in vertical direction
 * @type {{NATURAL: number, CLUSTERS: number}}
 */
progresio.utils.const.ordering = {
  TIME: "timePoint",
  SIMILARITY: "orderPos"
}

/**
 * type of scaling in horizontal direction
 * @type {{UNIFORM: number, TIME: number}}
 */
progresio.utils.const.scale = {
  TEMPORAL: "timeScale",
  ORDINAL: "ordinalScale",
  MDS_X: "mdsX",
  MDS_Y: "mdsY",
  ISO_X: "isoX",
  ISO_Y: "isoY"
}

progresio.utils.const.similarity = {
  CLASSIC: "value",
  T_SIM: "isoValue"
}

/**
 * this namespace is for the global control for progresio
 *  @namespace
 *  */
progresio.utils.config = progresio.utils.config || {};


progresio.utils.config.getConfiguration = function () {
  var configuration = {
    power: 2,
    x_scale: progresio.utils.const.scale.ORDINAL,
    x_order: progresio.utils.const.ordering.TIME,
    y_scale: progresio.utils.const.scale.ORDINAL,
    y_order: progresio.utils.const.ordering.SIMILARITY,
    similarity: progresio.utils.const.similarity.CLASSIC,
    rotate: 0,
    remove_overlap: true,
    min_distance: 9
  }
  return configuration;
}


/**
 * This part of codes are borrowed from dat.gui.js
 */
progresio.utils.common = (function () {

  var ARR_EACH = Array.prototype.forEach;
  var ARR_SLICE = Array.prototype.slice;

  /**
   * Band-aid methods for things that should be a lot easier in JavaScript.
   * Implementation and structure inspired by underscore.js
   * http://documentcloud.github.com/underscore/
   */

  return {

    BREAK: {},

    extend: function (target) {

      this.each(ARR_SLICE.call(arguments, 1), function (obj) {

        for (var key in obj)
          if (!this.isUndefined(obj[key]))
            target[key] = obj[key];

      }, this);

      return target;

    },

    defaults: function (target) {

      this.each(ARR_SLICE.call(arguments, 1), function (obj) {

        for (var key in obj)
          if (this.isUndefined(target[key]))
            target[key] = obj[key];

      }, this);

      return target;

    },

    compose: function () {
      var toCall = ARR_SLICE.call(arguments);
      return function () {
        var args = ARR_SLICE.call(arguments);
        for (var i = toCall.length - 1; i >= 0; i--) {
          args = [toCall[i].apply(this, args)];
        }
        return args[0];
      }
    },

    each: function (obj, itr, scope) {


      if (ARR_EACH && obj.forEach === ARR_EACH) {

        obj.forEach(itr, scope);

      } else if (obj.length === obj.length + 0) { // Is number but not NaN

        for (var key = 0, l = obj.length; key < l; key++)
          if (key in obj && itr.call(scope, obj[key], key) === this.BREAK)
            return;

      } else {

        for (var key in obj)
          if (itr.call(scope, obj[key], key) === this.BREAK)
            return;

      }

    },

    defer: function (fnc) {
      setTimeout(fnc, 0);
    },

    toArray: function (obj) {
      if (obj.toArray) return obj.toArray();
      return ARR_SLICE.call(obj);
    },

    isUndefined: function (obj) {
      return obj === undefined;
    },

    isNull: function (obj) {
      return obj === null;
    },

    isNaN: function (obj) {
      return obj !== obj;
    },

    isArray: Array.isArray || function (obj) {
      return obj.constructor === Array;
    },

    isObject: function (obj) {
      return obj === Object(obj);
    },

    isNumber: function (obj) {
      return obj === obj + 0;
    },

    isString: function (obj) {
      return obj === obj + '';
    },

    isBoolean: function (obj) {
      return obj === false || obj === true;
    },

    isFunction: function (obj) {
      return Object.prototype.toString.call(obj) === '[object Function]';
    }

  };

})();