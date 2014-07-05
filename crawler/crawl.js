var request = require('request');
var cities = ["8514", //"Berlin",
"9644", //"Hamburg",
"3577", //"München",
"15481", //"Köln",
"15475" //"Düsseldorf",
             ];

/*
for (var i = 0; i < cities.length; i++) {
    dumpHotels(cities[i]);
}*/

dumpHotels(cities[0], "Berlin");

function dumpHotels(cityCode, cName) {
    var url = 'http://www.trivago.de/?aDateRange%5Barr%5D=2014-07-27&aDateRange%5Bdep%5D=2014-07-28&iRoomType=7&iPathId=' + cityCode + '&iGeoDistanceItem=0&iViewType=0&bIsSeoPage=false&bIsSitemap=false&';

    console.log(url);

    request.get(url, function (error, response, body) {
        if (!error && response.statusCode == 200) {
            var env = require('jsdom').env;
            // first argument can be html string, filename, or url
            env({
                html: body,
                scripts: [],
                done: function (errors, window) {

                    var $ = require('jquery')(window);
                    var hotels = [];
                    var elems = $('#js_itemlist li');

                    elems.each(function (i) {
                        var l = $(this);
                        var img = l.find('.item_image img').attr('src');
                        var name = l.find('.item_prices h3').text();
                        var rating = l.find('.item_main .item_category .img_sprite_moon').text();

                        console.log(l);

                        var hotel = {
                            image: img,
                            name: name,
                            address: cName,
                            rating: rating
                        };
                        console.log(hotel);
                    });

                }

            });
        }
    });
}