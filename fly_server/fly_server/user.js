var mongoose = require('mongoose');
var Schema = mongoose.Schema;

/*var User = mongoose.model('User', {
name: String,
image: String,
miles: Number,
lat: Number,
long: Number
});*/
var userSchema = new Schema({
    name: String,
    image: String,
    miles: Number,
    lat: Number,
    lng: Number
});
module.exports = mongoose.model('User', userSchema);
//# sourceMappingURL=user.js.map
