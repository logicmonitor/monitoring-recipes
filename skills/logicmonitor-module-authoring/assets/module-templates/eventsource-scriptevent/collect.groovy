import groovy.json.JsonOutput

print JsonOutput.toJson([
    events: [[
        happenedOn: new Date().format("yyyy-MM-dd'T'HH:mm:ssZ"),
        severity: 'warn',
        message: 'Example script event'
    ]]
])
return 0
