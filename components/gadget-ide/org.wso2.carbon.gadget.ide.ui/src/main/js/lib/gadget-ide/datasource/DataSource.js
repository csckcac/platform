/**
 * @fileoverview Defines an abstract interface DataSource.
 * which is the basic unit in a data flow.
 *
 * @author rcmperea@gmail.com (Manuranga)
 */
goog.provide('gadgetide.datasource.DataSource');

/**
 * Interface for a DataSource.
 * DataSource is a unit in data flow. All the classes that retrieve or process data
 * that will end up in a user visible element should implement this interface.
 *
 * @param {!Object} config The configuration as a JSON object. this should contain the
 *     sufficient information to completely (de)serialize the DataField.eg:- all the user inputs
 *     specifying the DataSource.
 * @constructor
 * //interface
 */
gadgetide.datasource.DataSource = function(config) {
};

/**
 * dose the actual retrieve of data and/or processing of data.
 * this method may do an IO call like ajax call, web service call (using JSStub), ect.
 * but the call should be asynchronous and non-blocking(parallel calls should be possible).
 *
 * this method is guaranteed to be called after {@see gadgetide.datasource.DataSource#loadInputFormat} has been
 * called (and complected the callback) at least one time.
 *
 * @param {function(Object,string=): void} callback will be called when the execution is over and
 *     the result is ready.following arguments will be passed to callback.
 *
 *         (Object) result: data that is produced by execution should be passed as the first argument.
 *             this argument will be {@code null} if and only if there was an error during execution
 *
 *         (string=) opt_errorMsg:  string contains and error message if something went wrong
 *             during execution. this should be only present if the first argument is null
 *
 * @param {Object} input this is json object specifying input values for the execute
 *     it should agree with the structure specified by the {@code getInputFormat}.
 * @param {Object} context Run time context of for DataSource.
 * @return {void} Nothing.
 */
gadgetide.datasource.DataSource.prototype.execute = function(callback, input, context) {
};

/**
 * defines the input format(schema) of this {@code DataSource}.this should not be changed over time.
 *
 * @param {function(?gadgetide.schema.DSSchema): void} callback will be called when input data format is known.
 *     following arguments will be passed to callback.
 *
 *         (gadgetide.schema.DSSchema) format Structure of the input as <i>BadgerFish</i> transformation of the equivalent xml.
 *             this object's values should not be changed.if it's required to be changed, clone it.
 *
 * @return {void} Nothing.
 */
gadgetide.datasource.DataSource.prototype.loadInputFormat = function(callback) {
};

/**
 * defines the output format(schema) of this {@code DataSource}.this should not be changed over time.
 * format's structure is similar to that of {@see gadgetide.datasource.DataSource#loadInputFormat}
 *
 * this method is guaranteed to be called after {@see gadgetide.datasource.DataSource#loadInputFormat} has been
 * called (and complected the callback) at least one time.
 *
 * @param {function(?gadgetide.schema.DSSchema): void} callback will be called when output data format is known.
 *     following arguments will be passed to callback.
 *
 *         (gadgetide.schema.DSSchema) format Structure of the output as <i>BadgerFish</i> transformation of the equivalent xml.
 *             this object's values should not be changed.if it's required to be changed, clone it.
 *
 * @param {?gadgetide.schema.DSSchema} inputFormat This is a clone of the object returned by
 *     {@see gadgetide.datasource.DataSource#loadInputFormat}. but it will not have the same <b>anttype</b>
 *     attributes. instead they will be replaced by the actual formats.
 *
 * @return {void} Nothing.
 */
gadgetide.datasource.DataSource.prototype.loadOutputFormat = function(callback, inputFormat) {
};

