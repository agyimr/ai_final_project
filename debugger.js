const child_process = require("child_process");
const net = require('net');

setTimeout(, function () {}, 50000);

if(process.argv[2] == "--launcher") {
	
	var ps = child_process.spawn("java", ['-jar', 'server.jar', '-l', process.argv[3], '-c', "node debugger.js " + process.argv[5] + " " + process.argv[6] + " " + process.argv[7] + " " + process.argv[8], '-g', process.argv[4]]);

} else {
		
	errPort = process.argv[2];
	outPort = process.argv[3];
	inPort = process.argv[4];
	ioPort = process.argv[5];

	const errClient = net.createConnection({ port: errPort });
	const outClient = net.createConnection({ port: outPort });
	const inClient = net.createConnection({ port: inPort });
	const ioClient = net.createConnection({ port: ioPort });

	errClient.pipe(ioClient);
	//outClient.pipe(ioClient);
	
	errClient.pipe(process.stderr);
	outClient.pipe(process.stdout);
	process.stdin.pipe(inClient);
	
}
