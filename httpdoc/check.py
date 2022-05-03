import os
import sys

method = os.getenv("REQUEST_METHOD")
print("<html><body>")
print("<H1> check program </H1>")
print("<p>" + method)
print("<p> get Parameters:")
print("<ul>")

if method == "GET":
    query = os.getenv("QUERY_STRING")
    parameters = query[1:].split("&")
    for para in parameters:
        print("<li>" + para + "</li>")
elif method == "POST":
    parameters = sys.stdin
    for para in parameters:
        print("<li>" + para + "</li>")

print("</ul>")
print("</body></html>")