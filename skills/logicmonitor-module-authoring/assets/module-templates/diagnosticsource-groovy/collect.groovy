import groovy.json.JsonOutput

print JsonOutput.toJson([data: 'Diagnostic output', format: 'markdown'])
return 0
