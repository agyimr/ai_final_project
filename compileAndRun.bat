if "%~1" NEQ "" (
	cls && javac sampleclients\*.java && java -jar server.jar -l %~1 -c "java sampleclients.RandomWalkClient" -g 100 && taskkill /im java.exe /f
) else (
	cls && javac sampleclients\*.java && java -jar server.jar -l levels/MAthomasAppartment.lvl -c "java sampleclients.RandomWalkClient" -g && taskkill /im java.exe /f

)

