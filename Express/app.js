var net = require('net');
const tcp_port = 4321;
const http_port = 8080;

// http server
var promise = require('promise');
var express = require('express');
var app = express();
var path = require('path');
var bodyParser = require('body-parser');
var async = require('async');

app.locals.pretty = true;

app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

app.use(express.static('public'));
app.use(bodyParser.urlencoded({ extended: false }));

app.get('/', function (req, res) {
    res.render('index', {
        results : [],
        tablehead : [],
        link_name : 'https://www.naver.com/'
    });
});

app.post('/api/result', function(req, res) {
    async.waterfall([
        function(callback) {
            var jsonData = {
                searchFor : req.body.searchFor
            };
            socket = net.connect({ port: tcp_port, host: '127.0.0.1' });
            console.log('socket connected');
            callback(null, jsonData, socket);
        },
        function(data, socket, callback) {
            socket.write(JSON.stringify(data) + '\n');
            console.log('write data to server');
            var chunks = [];

            socket.on('data', function(chunk) {
                chunks.push(chunk);
            });

            socket.on('end', function() {
                var rcvData = Buffer.concat(chunks);
                rcvData = JSON.parse(rcvData);
                callback(null, rcvData);
            });
        },
        function (data, callback) {
            if (data === undefined) {
                callback('undefined info', 'error');
            }
            else {
                console.log(data.link_name);
                res.render('index', data);
                callback(null, 'render successful');
            }
        }
    ], function(err, result) {
        if (err === null) {
            console.log('run successful : ' + result);
        }
        else {
            console.log('error occurred : ' + error);
        }
    });
});

app.listen(http_port, function () {
    console.log('http server> listening on port 8080...');
});