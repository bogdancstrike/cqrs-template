#!/bin/bash

# Script to test the Alert Management System API endpoints.
# Make sure your Spring Boot application is running.
# This script requires 'jq' for parsing JSON responses: sudo apt-get install jq (or brew install jq on macOS)

BASE_URL="http://localhost:7676/api/v1/alerts" # Your application's port
ALERT_ID="" # Will be populated after creating an alert

# Function to make requests and print output
# Diagnostic output goes to stderr, function's "return" (for capture) goes to stdout
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local description="$4"
    local response_body
    local http_status
    local CURL_OUTPUT_FILE

    CURL_OUTPUT_FILE=$(mktemp) # Create a temporary file to store curl body output

    # Print diagnostics to stderr
    echo "-----------------------------------------------------" >&2
    echo "Test: $description" >&2
    echo "Request: $method $url" >&2
    if [ -n "$data" ]; then
        echo "Data: $data" >&2
    fi

    # -s for silent, -w "%{http_code}" to output HTTP status code to stdout after body
    # -o "$CURL_OUTPUT_FILE" to write body to a temporary file
    # The http_status will be captured from the last line of curl's stdout due to -w
    if [ "$method" == "POST" ] || [ "$method" == "PUT" ]; then
        http_status=$(curl -s -w "%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url" -o "$CURL_OUTPUT_FILE")
    else
        http_status=$(curl -s -w "%{http_code}" -X "$method" "$url" -o "$CURL_OUTPUT_FILE")
    fi

    response_body=$(cat "$CURL_OUTPUT_FILE")
    rm "$CURL_OUTPUT_FILE" # Clean up temporary file

    echo "HTTP Status: $http_status" >&2
    echo "Response Body (for display):" >&2
    if command -v jq >/dev/null 2>&1; then
        echo "$response_body" | jq . >&2
    else
        echo "$response_body" >&2 # Raw output if jq not available
    fi
    echo "-----------------------------------------------------" >&2
    echo "" >&2

    # This is the actual "return" value of the function for command substitution
    # It should only be the response body.
    echo "$response_body"
}

# --- Command Endpoints ---

# 1. Create a new alert
echo ">>> Testing CREATE Alert Endpoint <<<" >&2 # Send script's own logs to stderr
create_payload='{
  "severity": "HIGH",
  "description": "Automated API Test: Network Outage Detected",
  "source": "APITestScript",
  "details": {
    "region": "us-east-1",
    "service": "core-network"
  },
  "eventTimestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%SZ")'",
  "initiatedBy": "test-script-user"
}'
create_response=$(make_request "POST" "$BASE_URL" "$create_payload" "Create a new alert")

# Extract alertId if creation was successful (jq dependent)
if command -v jq >/dev/null 2>&1; then
    # Attempt to parse the create_response as JSON
    if echo "$create_response" | jq -e . > /dev/null 2>&1; then
        ALERT_ID=$(echo "$create_response" | jq -r '.alertId // empty')
    else
        echo "ERROR: Create alert response was not valid JSON. Response was:" >&2
        echo "$create_response" >&2
        ALERT_ID=""
    fi
else
    echo "WARNING: jq is not installed. Cannot automatically extract alertId." >&2
    echo "Please manually set ALERT_ID if you want to run subsequent tests." >&2
    # Example: ALERT_ID="your-manually-copied-alert-id"
fi

if [ -z "$ALERT_ID" ]; then
    echo "ERROR: Failed to create alert or extract alertId. Halting script." >&2
    exit 1
else
    echo "Successfully created alert with ID: $ALERT_ID" >&2
    echo "" >&2
fi

# Wait a moment for event processing and projection
sleep 2

# --- Query Endpoints (using the created alert) ---
echo ">>> Testing QUERY Alert Endpoints <<<" >&2

# 2. Get the created alert by ID
make_request "GET" "$BASE_URL/$ALERT_ID" "" "Get alert by ID: $ALERT_ID" > /dev/null # Discard display output for this one

# --- More Command Endpoints (using the created alert) ---
echo ">>> Testing more COMMAND Alert Endpoints for ID: $ALERT_ID <<<" >&2

# 3. Update the alert
update_payload='{
  "severity": "CRITICAL",
  "description": "Automated API Test: Network Outage - Escalated",
  "details": {
    "region": "us-east-1",
    "service": "core-network",
    "escalation_level": "2"
  },
  "updatedBy": "test-script-updater"
}'
make_request "PUT" "$BASE_URL/$ALERT_ID" "$update_payload" "Update alert: $ALERT_ID" > /dev/null
sleep 1

# 4. Acknowledge the alert
ack_payload='{
  "acknowledgedBy": "ops-team-member",
  "notes": "Initial investigation started."
}'
make_request "POST" "$BASE_URL/$ALERT_ID/acknowledge" "$ack_payload" "Acknowledge alert: $ALERT_ID" > /dev/null
sleep 1

# 5. Add a note to the alert
note_payload='{
  "text": "Router R7 appears to be offline.",
  "author": "net-admin"
}'
make_request "POST" "$BASE_URL/$ALERT_ID/notes" "$note_payload" "Add note to alert: $ALERT_ID" > /dev/null
sleep 1

# 6. Assign the alert
assign_payload='{
  "assignee": "john.doe",
  "assignedBy": "supervisor"
}'
make_request "POST" "$BASE_URL/$ALERT_ID/assign" "$assign_payload" "Assign alert: $ALERT_ID" > /dev/null
sleep 1

# 7. Resolve the alert
resolve_payload='{
  "resolvedBy": "net-engineer",
  "resolutionDetails": "Router R7 rebooted and connectivity restored."
}'
make_request "POST" "$BASE_URL/$ALERT_ID/resolve" "$resolve_payload" "Resolve alert: $ALERT_ID" > /dev/null
sleep 1

# 8. Close the alert
close_payload='{
  "closedBy": "system-auto-close",
  "reason": "Issue resolved and verified."
}'
make_request "POST" "$BASE_URL/$ALERT_ID/close" "$close_payload" "Close alert: $ALERT_ID" > /dev/null
sleep 1

# --- General Query Endpoints ---
echo ">>> Testing General QUERY Alert Endpoints <<<" >&2

# 9. Get all alerts (first page)
make_request "GET" "$BASE_URL?page=0&size=5" "" "Get all alerts (page 0, size 5)" > /dev/null

# 10. Find alerts by keyword "Automated"
make_request "GET" "$BASE_URL/search?keyword=Automated&page=0&size=5" "" "Find alerts by keyword 'Automated'" > /dev/null

# 11. Find alerts by status "RESOLVED"
make_request "GET" "$BASE_URL/status?status=RESOLVED&page=0&size=5" "" "Find alerts by status 'RESOLVED'" > /dev/null

# 12. Find alerts by timestamp range (adjust as needed)
# Get current time and time 10 minutes ago in ISO 8601 format
# Note: date command syntax might vary slightly between GNU/Linux and macOS
if date --version >/dev/null 2>&1 ; then # GNU date
    START_TIME=$(date -u -d '10 minutes ago' +"%Y-%m-%dT%H:%M:%SZ")
    END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
else # macOS date
    START_TIME=$(date -u -v-10M +"%Y-%m-%dT%H:%M:%SZ")
    END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
fi
make_request "GET" "$BASE_URL/filter?startTime=$START_TIME&endTime=$END_TIME&page=0&size=5" "" "Find alerts by timestamp range" > /dev/null

# --- Deletion (Logical Delete) ---
# This is usually the last step for a specific alert in a test sequence
echo ">>> Testing DELETE Alert Endpoint for ID: $ALERT_ID <<<" >&2

# 13. Delete the alert (logically)
make_request "DELETE" "$BASE_URL/$ALERT_ID?deletedBy=test-script-deleter&reason=TestCleanup" "" "Delete alert: $ALERT_ID" > /dev/null
sleep 1

# Verify it's marked as DELETED (or however your system handles it)
make_request "GET" "$BASE_URL/$ALERT_ID" "" "Verify deleted alert by ID: $ALERT_ID (should show DELETED status or be 404 if hard deleted)" > /dev/null

echo "" >&2
echo "API Test Script Finished." >&2
echo "Note: Some operations are asynchronous due to event sourcing." >&2
echo "Sleeps have been added, but you might need to adjust them or check logs/DB/Elasticsearch for confirmation." >&2
