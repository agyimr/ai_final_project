var child_process = require("child_process");
var fs = require("fs");

const t = new Date();

var directory = t.toISOString().substr(0,10) + "_" + t.toTimeString().substr(0, 8).replace(/:/g, ".");

fs.mkdirSync("./benchmarks/" + directory);

const csvFilename = directory + "/benchmark.csv";
const SEP = ";";

var csv = "";

function benchmark(lvl, algorithm) {


	const f = "./benchmarks/" + directory + "/mark-" + lvl + "-" + algorithm + ".log"

	fs.unwatchFile(f);
	fs.writeFileSync(f, "");	
	
	var ps = child_process.exec("java -jar server.jar -l levels/"+lvl+" -c \"java -Xmx4g searchclient.SearchClient -"+algorithm+"\" -g 0 -t 180 > "+f+" 2>&1");

	fs.watchFile(f, (curr, prev) => {
		
		var buffer = fs.readFileSync(f).toString();
				
		if(!buffer.match("Runner completed")) {
			return;
		}

		try {
			
			child_process.execSync("taskkill /im java.exe /f", {"stdio" : "ignore"});	
			
		} catch(e) {}
		
		setTimeout(function () {

			const reports = buffer.match(/#Explored[^\n]+/gm);
			
			const matchSolution = buffer.match(/Found solution of length ([0-9,? ]+)/);

			const solutionLength = (matchSolution != null) ? matchSolution[1] : "-";	
			
			const maximumMemory = buffer.match(/Maximum/);
			
			const timeout = buffer.match(/timeout/) ? "timeout" : "";	
			
			if(reports != null && reports.length > 0) {
				
				const lastReport = reports[reports.length-1];			
				
				const matchMeasures = lastReport.match(/#Generated[^0-9�]+([0-9�]+),[^0-9,]+([0-9,�]+ s)[^0-9,�]+([0-9,�]+ MB)/);
				
				console.log(lvl, "\t\t", algorithm, "   \t\t", matchMeasures[2], "\t\t", (maximumMemory ? "Max" : matchMeasures[3]), "\t\t", solutionLength, "   \t\t", matchMeasures[1].replace(/�/g, " "), "\t", (timeout ? "Timeout" : ""), "");
				csv += lvl + SEP + algorithm + SEP +  matchMeasures[2] + SEP + (maximumMemory ? "Max" : matchMeasures[3]) + SEP + solutionLength + SEP + matchMeasures[1].replace(/�/g, " ") + SEP + (timeout ? "Timeout" : "") + "\n";
												
				fs.writeFileSync(csvFilename, csv);
				fs.unwatchFile(f);
			}			
			
			loop();
		
		}, 500);
		
	});
}
	
	
// lvls = ["SAsoko1_06", "SAsoko1_12", "SAsoko1_24", "SAsoko1_48", "SAsoko2_06", "SAsoko2_12", "SAsoko2_24", "SAsoko2_48", "SAsoko3_05", "SAsoko3_06", "SAsoko3_07", "SAsoko3_08", "SAsoko3_12", "SAsoko3_24", "SAsoko3_48"];
// lvls = ["SAD1", "SAD2", "friendofDFS", "friendofBFS", "SAFirefly", "SACrunch", "SAsoko1_48", "SAsoko2_48", "SAsoko3_48"];
// lvls = ["friendofBFS"];

// not_in = ["friendofBFS.lvl", "friendofDFS.lvl", "miniworld.lvl", "SAanagram.lvl", "SAbotbot.lvl", "SAboXboXboX.lvl", "SAchoice.lvl", "SACrunch.lvl", "SAD1.lvl", "SAD2.lvl", "SAD3.lvl", "SAFirefly.lvl", "SAlabyrinthOfStBertinEmpty.lvl", "SALazarus.lvl", "SAmicromouseBoxAtStart.lvl", "SAmicromouseContest2011.lvl", "SAOptimal.lvl", "SApacman.lvl", "SApushing.lvl", "SAsimple0.lvl", "SAsimple1.lvl", "SAsimple2.lvl", "SAsimple3.lvl", "SAsimple4.lvl", "SAsoko1_06.lvl", "SAsoko1_12.lvl", "SAsoko1_24.lvl", "SAsoko1_48.lvl", "SAsoko2_06.lvl", "SAsoko2_12.lvl", "SAsoko2_24.lvl", "SAsoko2_48.lvl", "SAsoko3_05.lvl", "SAsoko3_06.lvl", "SAsoko3_07.lvl", "SAsoko3_08.lvl", "SAsoko3_12.lvl", "SAsokobanLevel96.lvl", "SASolo.lvl", "SAtest.lvl", "SATheRedDot.lvl", "SAtowersOfHoChiMinh03.lvl", "SAtowersOfHoChiMinh04.lvl", "SAtowersOfHoChiMinh05.lvl", "SAtowersOfHoChiMinh10.lvl", "SAtowersOfHoChiMinh26.lvl", "SAtowersOfSaigon03.lvl", "SAtowersOfSaigon04.lvl", "SAtowersOfSaigon05.lvl", "SAtowersOfSaigon10.lvl", "SAtowersOfSaigon26.lvl"];

lvls = fs.readdirSync("levels");

console.log(lvls.length + " levels")

algorithms = ["astar", "greedy", "bfs", "dfs", "wastar"];
// algorithms = ["astar"];

configs = [];

for(const lvl of lvls) {
	
	// if(not_in.indexOf(lvl) != -1) {
		// continue;
	// }
	
	for(const algorithm of algorithms) {
		
		configs.push({lvl : lvl, algorithm : algorithm});
		
	}	
	
}

function loop() {
	
	var config = configs.shift();
	
	if(config != null) {
		
		console.log(config.lvl, config.algorithm);
		benchmark(config.lvl, config.algorithm);
	}
	
	
	
}
	
loop();