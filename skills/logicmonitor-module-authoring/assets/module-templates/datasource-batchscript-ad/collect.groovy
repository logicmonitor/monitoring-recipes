// Template: batchscript collection — emit instance.key=value per line
def instance = instanceProps.get('wildvalue') ?: 'default'
println "${instance}.example_metric=0"
return 0
