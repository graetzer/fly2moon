var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var hotelSchema = new Schema({
    name: String,
    lat: Number,
    lng: Number,
    image: String,
    description: String,
    rating: Number,
    comments: [String],
    adress: String
});

/*var Hotel = mongoose.model('Booking', {
name: String,
lat: Number,
lng: Number,
image: String,
description: String,
rating: Number,
comments: [String]
});*/
module.exports = mongoose.model('Hotel', hotelSchema);
//# sourceMappingURL=hotel.js.map
