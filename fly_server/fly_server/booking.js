var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var bookingSchema = new Schema({
    hotelID: mongoose.Schema.Types.ObjectId,
    date: Date,
    userID: mongoose.Schema.Types.ObjectId,
    washere: Boolean
});

module.exports = mongoose.model('Booking', bookingSchema);
//# sourceMappingURL=booking.js.map
