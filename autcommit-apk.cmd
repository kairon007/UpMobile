set dropbox=e:\dropbox
set link=%dropbox%\������\apk\%1.apk
del "%link%"
mklink "%link%" "%~dp0\%1\bin\%1.apk"