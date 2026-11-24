@echo off
cd /d E:\Projects\Tank_Working
git add .
set /p msg="Commit message: "
git commit -m "%msg%"
git push
echo.
echo Done! Press any key to close.
pause > nul