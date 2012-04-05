goog.provide('gadgetide.datasource.Timer');

goog.require('goog.events.EventTarget');

/**
 * @param {number} delay
 * @extends {goog.events.EventTarget}
 * @constructor
 */
gadgetide.datasource.Timer = function(delay) {
  goog.events.EventTarget.call(this);
  //TODO: use goog.Timer
  this.intervalId_ = goog.global.setInterval(goog.bind(this.fireTimerEvent_, this)
    , delay);
};
goog.inherits(gadgetide.datasource.Timer, goog.events.EventTarget);


/**
 * fires the TIMER_EVENT.
 */
gadgetide.datasource.Timer.prototype.fireTimerEvent_ = function() {

  goog.events.dispatchEvent(this,
    gadgetide.datasource.Executor.EventType.TIMING_EVENT);
};

gadgetide.datasource.Timer.prototype.stop = function() {
  goog.global.clearInterval(this.intervalId_);
};

