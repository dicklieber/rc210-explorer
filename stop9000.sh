#/bin/bash
#lsof -nP -iTCP -sTCP:LISTEN | grep 9000
#todo add kill for process

pid=$(lsof -nP -iTCP -sTCP:LISTEN | grep 9000 | awk '{print $2}')
if [ -z "$pid" ]
then
  echo "No pid listening to port 9000"
else
  kill $pid
fi