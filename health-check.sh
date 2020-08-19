# Checks that the value specified in parameter 2 is found in the response from the URL passed in parameter 1

echo "GLA-OPS Server health-check | $1 | $2 "

for page in {1..36}
do
    echo "Sending request..."
	response=$(curl -s $1)
	echo "Response received."
	if [[ "$response" == *"$2"* ]]; then
	    echo "  Found '$2' in response from $1"
	    echo "  Server is up"
	    exit 0
	else
	    echo "  Did not find '$2' in $response"
        echo "Waiting for 5 seconds..."
	sleep 5
    fi
done
echo "SERVER IS STILL DOWN AFTER 3 MINUTES!!!"
exit 1