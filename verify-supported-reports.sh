echo "GLA-OPS Server verifying supported reports | $2 "

echo "Verifying supported reports ..."

response=$(curl --user $1 -s $2)
echo "Response received."
if [[ "$response" == *"0"* ]]; then
    echo "  Verification successful"
    exit 0
else
    echo "  Reports verification failure"
    exit 1
fi
