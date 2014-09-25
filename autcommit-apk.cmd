set dropbox=e:\dropbox
set link=%dropbox%\Ангелы\apk\%1.apk
del "%link%"
mklink "%link%" "%~dp0\%1\bin\%1.apk"