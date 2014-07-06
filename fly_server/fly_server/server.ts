/// <reference path="definitions/node.d.ts" />
/// <reference path="definitions/express.d.ts" />


//declare function module(name: string);
//declare var ObjectId;
import express = require("express");
var mongoose = require('mongoose');
var User = require('./user.js');
var Booking = require('./booking.js');
var Hotel = require('./hotel.js');
var nQuery = require('nodeQuery');
var distance = require('gps-distance');

var bodyParser = require('body-parser');

var app = express();

app.all('/', function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    res.header('Access-Control-Allow-Methods', 'GET, POST');//, OPTIONS, PUT, PATCH, DELETE');

    next();
});

app.get('/', function (req, res, next) {
    // Handle the get for this route
});

app.post('/', function (req, res, next) {
    // Handle the post for this route
});

app.use(bodyParser.json());       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded());




mongoose.connect("mongodb://headgame_mongoadmin:gomhajPipp@localhost:20604/test", { auth: { authdb: "admin" } });
//Hotel.update({ image: 'http://imgec.trivago.com/itemimages/10/04/100425_v2_mc.jpeg' }, { $set: { lat: 51.213770, lng: 6.753750 } });
//Booking.update({ _id: mongoose.Types.ObjectId("53b877444015dc8c5d42cf45") }, { $set: { date: 1405644284406 } }, {}, (err, a) => { console.log(err);});
//Booking.update({ _id: mongoose.Types.ObjectId("53b9162071b25c120ffcd56c") }, { $set: { date: 1405645284406 } }, {}, (err, a) => { console.log(err); });

console.log('Starting server..');
app.get('/user', function (req, res) {    

    User.find({ _id: mongoose.Types.ObjectId("53b81a2f034eb5db75ec16ad")}, function (err, user) {
        if (err) return console.log(err);
        res.send(user);
    });
    console.log('get user');
});

app.get('/hotel', function (req, res) {
    var max_dist = req.query.distance == undefined ? 5000000 : req.query.distance;
    
    if (req.query.hotelID != undefined) {
        try {
            mongoose.Types.ObjectId(req.query.hotelID);
        } catch (e) {
            res.send([]);
            return;
        }
        Hotel.find({ _id: mongoose.Types.ObjectId(req.query.hotelID)}, (err, hotels) => {
            res.send(hotels);
        });
        return;
    }

    Hotel.find({}, (err, hotels: any[]) => {
        var results = [];
        //console.log("request:", req.query);
        if (req.query.lng == undefined || req.query.lat == undefined) {
            res.send(results);
            return;
        } 
        //console.log("hotels:", hotels);
        hotels.forEach((hotel) => {
            var dist = distance(parseFloat(req.query.lat), parseFloat(req.query.lng), hotel.lat, hotel.lng);
            // console.log("dist",dist);
            //hotel.dist = dist;
            //console.log("hotel.dist", hotel.dist);
            if (dist <= max_dist) {
                results.push({hotel: hotel, dist: dist });                  
            }
        });
        
        if (err) return console.log(err);
        results.sort((a, b) => { return a.dist - b.dist; });

        for (var i = 0; i < results.length; i++)results[i] = results[i].hotel;
        res.send(results);
    });
    
    console.log('get hotel', req.body);
});

app.get('/booking', function (req, res) {
    Booking.find({ userID: mongoose.Types.ObjectId("53b81a2f034eb5db75ec16ad")}, function (err, booking:any[]) {
        if (err) return console.log(err);
        res.send(booking);
    });

    console.log('get booking',req.body);
});



app.post('/booking', function (req, res) {
    
    console.log('received POST setBooking', req.body); 
    if (!req.body.userID)  
        req.body.userID = "53b81a2f034eb5db75ec16ad";
    if (!req.body.hotelID)
        req.body.hotelID = "53b813a136759b3169d2f8f8";
    if (!req.body.date)
        req.body.date = new Date().getTime();


    var booking = new Booking(req.body );
    booking.save(function (err) {
        if (err) {
            console.log('booking error');
            res.send('Booking error');
        } else res.send('Booking ok.');
            
    });
});

app.post('/hotel', function (req, res) {

    console.log('received Hotel', req.body);
    //req.body.userID = "53b81bf05097c15b0f8defee";

    var hotel = new Hotel(req.body);
    hotel.save(function (err) {
        if (err) {
            console.log('booking error');
            res.send('Booking error');
        } else res.send('Booking ok.');
    });
});

var server = app.listen(57640, function () {
    console.log('Listening on port %d', server.address().port);
});

/*

Hotel.findOne({ 'name': 'Park Inn' }, function (err, hotel) {
    if (err) return console.log(err);
    console.log('hotel', hotel) // Space Ghost is a talk show host.
    User.findOne({ 'name': 'Zildjian' }, function (err, user) {
        if (err) return console.log(err);
        console.log('user', user) // Space Ghost is a talk show host.

        var booking = new Booking({
            hotelID: [hotel._id],
            date: new Date(),
            userID: [user._id],
            washere: false
        });

        booking.save(function (err) {
            if (err) {
                console.log('booking error');

            } else console.log('booking ok');
        });

})

})

/*
var user = new User({
    name: 'Zildjian',
    image: 'http://idiap.github.io/bob/img/github.png',
    miles: 15,
    lat: 1.24556897,
    lng: 3.2118656
});

user.save(function (err) {
    if (err) {
        console.log('user error');

    } else console.log('user ok');
});



var user = new User({
    name: 'Zildjian',
    image: 'http://idiap.github.io/bob/img/github.png',
    miles: 15,
    lat: 1.24556897,
    lng: 3.2118656
});

user.save(function (err) {
    if (err) {
        console.log('user error');

    } else console.log('user ok');
});


var request = require('request');
request.get('http://www.trivago.de/?aDateRange%5Barr%5D=2014-07-27&aDateRange%5Bdep%5D=2014-07-28&iRoomType=7&iPathId=8514&iGeoDistanceItem=0&iViewType=0&bIsSeoPage=false&bIsSitemap=false&', function (error, response, body) {
    if (!error && response.statusCode == 200) {
        var csv = body;
        //var a = jQuery(csv);
       // console.log(a);
        // Continue with your processing here.
    }
});*/
/*
var hotel = new Hotel({
    name: 'Park Inn',
    lat: 52.5225491,
    lng: 13.4128035,
    image: 'http://imgec.trivago.com/itemimages/57/42/5742_v3_mc.jpeg',
    description: '',
    rating: 4,
    comments: [],
    adress: 'Alexanderplatz 7, 10178, Berlin, Deutschland'
});
hotel.save(function (err) {
    if (err) {
        console.log('hotel error');
        
    } else console.log('hotel ok');
});*/
/*
Hotel.findOne({ 'name': 'Park Inn' },  function (err, hotel) {
    if (err) return console.log(err);
    console.log('hotel', hotel) // Space Ghost is a talk show host.



})



//mongoose.connect('mongodb://maler:unnormalerreichtum@localhost:20604');
/*
var Cat = mongoose.model('Cat', { name: String });

var kitty = new Cat({ name: 'Zildjian' });
kitty.save(function (err) {
    if (err) // ...
        console.log('meow');
}); */