const child_process = require("child_process");
const fs = require("fs");
const request = require("request");
const os = require("os");

const cwd = process.cwd().replace(/benchmark$/, "").replace(/\\/g, "/").replace(/\/+$/g, "") + "/";
const hostname = os.hostname() + " - " + os.userInfo().username;

function benchmark (level, callback) {

	var error = "OK";

	var start = null;
	var end = null;
	
	var jointActionsCounter = -3;

	var ps = child_process.spawn("java", ['-jar', 'server.jar', '-l', "levels/" + level, '-c', 'java -Xmx2g sampleclients.RandomWalkClient', '-t', '180'], {cwd : cwd});

	ps.stderr.on('data', function (data) {

		if(data.includes("Map too wide")) {
			error = "MAP_TOO_WIDE";
			ps.kill();
		}
	});
	
	ps.stdout.on('data', function (data) {

		if(end != null) {
			return;
		}
		
		if(start == null) {
			if(data.includes("Runner starting")) {
				start = Date.now();
			}
		}			
		
		const successMatch = data.indexOf("success");
		const timeoutMatch = data.indexOf("timeout");

		var limit = data.length;		
		
		if(successMatch != -1) {
			
			limit = successMatch;
			end = Date.now();						
			
		} else if(timeoutMatch != -1){
			limit = timeoutMatch;
			error = "TIMEOUT";
			end = Date.now();
		}
		
		var i = 0;

		for (const value of data.values()) {
			if(value == 0x0d && i++ < limit) {
				jointActionsCounter++;
			}
		}
		
		if(end != null) {
			ps.kill();
		}
		
	});


	ps.on('close', function (code) {
				
		callback(error, (end - start), jointActionsCounter);
	});

	ps.on('error', function (code) {
		console.log('Error : ' + code);
	});

}

const t = new Date();
const runId = t.toISOString().substr(0,10) + "_" + t.toTimeString().substr(0, 8).replace(/:/g, ".");

const clientDirectory = cwd + "sampleclients/";
const levelsDirectory = cwd + "levels/";
const benchmarksDirectory = cwd + "benchmark/benchmarks/";

const runDirectory = benchmarksDirectory + runId + "/";
const runSourcesDirectory = runDirectory + "src/";

const runSourcesFile = runId + "-src.zip";
const runResultsFile = runId + "-results.csv";

fs.mkdirSync(runDirectory);
fs.mkdirSync(runSourcesDirectory);

const clientFiles = fs.readdirSync(clientDirectory);

for(const clientFile of clientFiles) {
	
	if(clientFile.match(/\.java$/)) {
		fs.createReadStream(clientDirectory + clientFile).pipe(fs.createWriteStream(runSourcesDirectory + clientFile));
	}
	
}

child_process.execSync("jar -cMf ../" + runSourcesFile + " .", {"cwd" : runSourcesDirectory});

var formData = {
  file_id: "0",
  comment : "",  
  public : "1",  
  duree : "9",  
  mail : "",  
  USERFILE: {
    value:  fs.createReadStream(runDirectory + runSourcesFile),
    options: {
      filename: runSourcesFile,
      contentType: 'application/x-zip-compressed'
    }
  }
};

console.log("Versionning sources...");

request.post({url:'https://www.cjoint.com/upload.json', formData: formData}, function optionalCallback(err, httpResponse, body) {
	if (err) {
		return console.error('Upload sources failed:', err);
	}

	var hash = JSON.parse(body).clef;
	
	console.log("Benchmark session " + hash + "\n");
	
	const csvSep = ";";
	const csvEof = "\n";

	fs.writeFileSync(runDirectory + runResultsFile, "LEVEL" + csvSep + "LEVEL" + csvSep + "ERROR" + csvSep + "DURATION" + csvSep + "ACTIONS" + csvEof);

	const levels = fs.readdirSync(levelsDirectory);

	function loop() {
		
		var level = levels.shift();
		
		if(level != null) {
			console.log("Run " + level + " level");
			benchmark(level, function(error, duration, actions) {
				
				fs.appendFileSync(runDirectory + runResultsFile, hash + csvSep + level + csvSep + error + csvSep + duration + csvSep + actions + csvEof);

				request.post({url:'http://kyx.lescigales.org/aimasters/api/addRun.php', formData: {
					hash : hash,
					hostname : hostname,
					level : level,
					error : error,
					duration : duration,
					actions : actions,
					time : t.getTime()
				}});
				
				console.log("Result : " + duration + " ms for " + actions + " actions\n");
				
				loop();
			});		
		}
		
	}
		
	loop();	
});
