var rawLevel = "";

rawLevel += "++++++++++" + "\n";
rawLevel += "+   +    +" + "\n";
rawLevel += "+   +  + +" + "\n";
rawLevel += "+   +  + +" + "\n";
rawLevel += "+  ++  + +" + "\n";
rawLevel += "+   +  + +" + "\n";
rawLevel += "++  +  + +" + "\n";
rawLevel += "+      + +" + "\n";
rawLevel += "++++++++++";

console.log(rawLevel);

level = rawLevel.split("\n");
outputLevel = rawLevel.split("\n");

freeCases = [];

for(var j = 0; j < level.length; j++) {		
	
	var freeCasesLine = [];
	
	outputLevel[j] = outputLevel[j].split("");
	
	for(var i = 0; i < level[j].length; i++) {
		
		freeCasesLine.push(level[j][i] != "+");

	}
	
	freeCases.push(freeCasesLine);
}

rects = [];

rectId = 0;

for(var j = 0; j < level.length; j++) {			
	
	for(var i = 0; i < level[j].length; i++) {		
		
		if(freeCases[j][i]) {
			
			var ax = i;
			var ay = j;
			
			var bx = ax;
			var by = ay;		
			
			var horizontalExtensionPossible = true;
			var verticalExtensionPossible = true;
			
			while((horizontalExtensionPossible || verticalExtensionPossible) && bx < level[j].length && by < level.length) {
				
				if(horizontalExtensionPossible) {
					bx++;
					
					//console.log("Try", i, j, {ax : ax, ay : ay, bx : bx, by: by})
					
					for(var y = ay; y <= by; y++) {
						
						if(!freeCases[y][bx]) {
							horizontalExtensionPossible = false;
							bx--;
							break;
							console.log("Failed")
						}
						
					}
					
					for(var y = ay; y <= by; y++) {
						freeCases[y][bx] = false;
						outputLevel[y][bx] = rectId;
					}
				}
				
				if(verticalExtensionPossible) {
					by++;
					
					for(var x = ax; x <= bx; x++) {
						
						if(!freeCases[by][x]) {
							verticalExtensionPossible = false;
							by--;
							break;
						}
						
					}
					
					for(var x = ax; x <= bx; x++) {
						freeCases[by][x] = false;
						outputLevel[by][x] = rectId;
					}
				}			
				
			}
		
			outputLevel[ay][ax] = rectId;
		
			rects.push({ax : ax, ay : ay, bx : bx, by: by});
			
			i += (bx - ax);
			
			if(i > level[0].length) {
				i = 0;
				j++;
			}
			
			rectId++;
		}

	}
}

console.log("")

var outputDisplay = "";

for(var j = 0; j < level.length; j++) {	
	
	outputDisplay += outputLevel[j].join("") + "\n";

}

console.log(outputDisplay);