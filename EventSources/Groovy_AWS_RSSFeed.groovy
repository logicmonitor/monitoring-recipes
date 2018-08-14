import groovy.json.JsonOutput
import groovy.time.TimeCategory

import java.text.SimpleDateFormat

try
{
    // Get the RSS feed
    def rssFeed = "http://status.aws.amazon.com/rss/all.rss".toURL().text;

    // Parse the RSS feed into an object we can iterate over
    def rss = new XmlSlurper().parseText(rssFeed)

    // Create a map that we'll convert to a JSON object to print the events
    def json = [:];
    json['events'] = []

    // Iterate over each event in the feed
    rss.channel.item.each
    { event ->

        // Get current date and time
        thisRunTime = new Date()

        // Calculate last time the script ran
        use(TimeCategory)
        {
            lastRunTime = thisRunTime - 20.minutes
        }

        // Find out when this event was published
        eventPubTime = new SimpleDateFormat("E, dd MMM yyyy h:m:s z", Locale.ENGLISH).parse("$event.pubDate")

        // If the event was published since the last time the script ran, print it.
        if (eventPubTime.after(lastRunTime))
        {
            // Create temporary map for this event
            new_event = [:];
            new_event['happenedOn'] = event.pubDate.toString();
            new_event['severity'] = 'warn';
            new_event['message'] = event.title.toString();
            new_event['source'] = event.guid.toString();

            // Append this event into the JSON events map
            json['events'] << new_event;
        }
    }

    // Were there any new events?
    if (json['events'].size > 0)
    {
        // Yes, convert the json map to a JSON string and print it
        println JsonOutput.toJson(json);
    }

    // Exit script with return value 0.
    return (0);
}
catch (Exception e)
{
    println e
    return 1
}
