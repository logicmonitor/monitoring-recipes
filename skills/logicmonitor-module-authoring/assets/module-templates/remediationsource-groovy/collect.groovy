import groovy.json.JsonOutput

print JsonOutput.toJson([
    data: 'Remediation output',
    format: 'markdown',
    remediationStatus: 'true'
])
return 0
