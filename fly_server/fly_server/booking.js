var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var bookingSchema = new Schema({
    hotelID: mongoose.Schema.Types.ObjectId,
    date: Number,
    userID: mongoose.Schema.Types.ObjectId,
    washere: { type: Boolean, default: false }
});

module.exports = mongoose.model('Booking', bookingSchema);
//# sourceMappingURL=booking.js.map
