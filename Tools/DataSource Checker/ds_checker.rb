#!/usr/bin/ruby

##
## ds_checker.rb - check datasource for validity and style
##

# USAGE
#  ds_checker.rb acct user pass ds_instance_id
#  ds_checker.rb xmlfile

# HISTORY
#  v0.1  mdunham - initial concept
#  v0.2  mdunham - squished some bugs, added testing for custom alert message
#  tokens, added more comments
#  v0.3  mrodrigues - fixed bug ignoring dps in ographs
#  v0.4  mrodrigues - check search keyword tags, treat "IOPS" as valid y-axis
#  label

require 'net/http'
require 'openssl'
require 'nokogiri'


##
## check arguments
##

# how many arguments were provided?
if (ARGV.length == 4)
  # four - setup params for remote processing
  $account     = ARGV[0]
  $userid      = ARGV[1]
  $passwd      = ARGV[2]
  $instance_id = ARGV[3]
elsif (ARGV.length == 1)
  # one - setup params for file processing
  $input_file  = ARGV[0]
else
  # something else - throw an error
  puts "ERROR: " + ARGV.length.to_s + " arguments provided; either 1 or 4 needed\n"
  abort(
    "USAGE:\t" + $0 + " account userid passwd instance_id\n" +
    "\t" + $0 + " xmlfile\n"
  )
end

class DS_Test

  ## get_remote_datasource - get datasource from remote account
  ##   @param string account - lm account name
  ##   @param string userid  - account userid
  ##   @param string passwd  - account password
  ##   @param int instance_id - datasource instance number
  ##   @return string source_xml - datasource xml

  def get_remote_datasource(account, userid, passwd, instance_id)
    host = 'https://' + account + '.logicmonitor.com'
    endpoint = '/santaba/rest/setting/datasources/' + instance_id + '/exportxml'
    uri  = URI host + endpoint

    http = Net::HTTP.new(uri.host, uri.port)
    http.use_ssl = true
    http.verify_mode = OpenSSL::SSL::VERIFY_PEER

    request = Net::HTTP::Get.new(uri.request_uri)
    request.basic_auth userid, passwd
    response = http.request(request)

    # did we get an "ok" response?
    if (response.code.to_i > 200)
      # no -- throw error
      abort("ERROR: Unable to retrieve datasource from " + host + " -- got response " + response.code)
    end

    source_xml = response.body
    write_datasource_file(source_xml)

    return source_xml
  end

  ## get_local_datasource - get datasource from local file
  ##   @param string filename - filename of local xml
  ##   @return string source_xml - datasource xml

  def get_local_datasource(xmlfile)
    # does the provided xml filename exist?
    if (File.exist?(xmlfile))
      # yes. -- extract the xml
      source = open(xmlfile,'r')
      source_xml = source.read
      source.close
    else
      # no -- throw an error
      abort("ERROR: Unable to open file " + xmlfile)
    end

    return source_xml
  end

  ## get_datasource - get datasource object
  ##   @param void
  ##   @return object dsobj - datasource object

  def get_datasource()
    # was an input file arg provided?
    if ($input_file)
      # yes -- use local xml file as source
      source_xml = get_local_datasource($input_file)
    else
      # no -- get sourcex xml from account
      source_xml = get_remote_datasource($account, $userid, $passwd, $instance_id)
    end

    dsobj = Nokogiri::XML(source_xml)

    return dsobj
  end

  ## write_datasource_file - write remote datasource to local file
  ##   @param string filename - filename of local xml
  ##   @return void

  def write_datasource_file(dsxml)
    dsobj = Nokogiri::XML(dsxml)
    dshash = get_dshash(dsobj)

    filename = '/tmp/' + dshash['name'] + '.xml'

    # does the output file exist?
    if ( File.exists?(filename) )
      # yes -- write out an error message
      puts "* file " + filename + " already exists -- refusing to write"
    else
      # no. store file
      target = open(filename,'w')
      target.write(dsxml)
      target.close
      puts "* stored datasource file in " + filename
    end
  end

  ## get_dshash - build a hash containing datasource metadata objects
  ##   @param object dsobj - datasource object
  ##   @return object dshash - datasource hashtable

  def get_dshash(dsobj)
    dshash = Hash[
      'name' => dsobj.at_xpath('//feed/entry/name').text,
      'dname' => dsobj.at_xpath('//feed/entry/displayedas').text,
      'desc' => dsobj.at_xpath('//feed/entry/description').text,
      'applies' => dsobj.at_xpath('//feed/entry/appliesTo').text,
      'multi' => dsobj.at_xpath('//feed/entry/hasMultiInstances').text,
      'pollint_s' => dsobj.at_xpath('//feed/entry/schedule').text,
      'datapoints' => dsobj.xpath('//feed/entry/datapoints/datapoint'),
      'graphs' => dsobj.xpath('//feed/entry/graphs/graph'),
      'ographs' => dsobj.xpath('//feed/entry/overviewgraphs/overviewgraph'),
      'tags' => dsobj.xpath('//feed/entry/tags').text
    ]

    return dshash
  end

  ## get_dphash - build a hash containing datapoints
  ##   @param object dpobject - datapoint object
  ##   @return object dp_hash - datapoint hashtable

  def get_dphash(dpobj)
    dp_hash = Hash[
      'ppmethod'       => dpobj.at_xpath("postprocessormethod").text,
      'name'           => dpobj.at_xpath("name").text,
      'desc'           => dpobj.at_xpath("description").text,
      'ppparam'        => dpobj.at_xpath("postprocessorparam").text,
      'alert_trigger'  => dpobj.at_xpath("alertexpr").text,
      'alert_transint' => dpobj.at_xpath("alertTransitionIval").text,
      'alert_msgbody'  => dpobj.at_xpath("alertbody").text,
    ]

    return Hash[dp_hash.sort]
  end

  ## get_graphhash - build a hash containing graph objects
  ##   @param object dsobj - graph object
  ##   @return object graphhash - graph hashtable

  def get_graphhash(graphobj)
    graphhash = Hash[
      'display'        => graphobj.at_xpath("displayprio").text,
      'name'           => graphobj.at_xpath("name").text,
      'yaxis'          => graphobj.at_xpath("verticallabel").text,
      'datapoints'     => graphobj.xpath('graphdatapoints/graphdatapoint'),
    ]

    return Hash[graphhash.sort]
  end

  ## get_ographhash - build a hash containing ograph objects
  ##   @param object dsobj - ograph object
  ##   @return object ographhash - ograph hashtable

  def get_ographhash(ographobj)
    ographhash = Hash[
      'display'        => ographobj.at_xpath("displayprio").text,
      'name'           => ographobj.at_xpath("name").text,
      'yaxis'          => ographobj.at_xpath("verticallabel").text,
      'datapoints'     => ographobj.xpath('datapoints/overviewgraphdatapoint'),
    ]

    return Hash[ographhash.sort]
  end

  ## get_graphdphash - build a hash containing graph datapoint objects
  ##   @param object dsobj - graph datapoint object
  ##   @return object graphhash - graph hashtable

  def get_graphdphash(graph_datapoint_obj)
    graphdphash = Hash[
      'name'      => graph_datapoint_obj.at_xpath("name").text,
      'dpname'    => graph_datapoint_obj.at_xpath("datapointname").text,
    ]

    return graphdphash
  end

  ## print_summary - print the datasource summary report section
  ##   @param object dsobj - datasource object
  ##   @return void

  def print_summary(dsobj)
    dshash = get_dshash(dsobj)
    ds_pollint_m = dshash['pollint_s'].to_i / 60
    dp_alert_count = 0.to_i

    # iterate over each datapoint
    dshash['datapoints'].each { |datapoint|
      dphash   = get_dphash(datapoint)

      # is the alert trigger set on this datapoint
      if ( dphash['alert_trigger'].size > 0 )
        # yes, increment the alert count var
        dp_alert_count += 1
      end

    }

    puts "Summary:\n"

    puts " - datasource name:\t" + dshash['name'] + "\n"
    puts " - display name:\t" + dshash['dname'] + "\n"
    puts " - applies to:\t\t" + dshash['applies'] + "\n"
    puts " - search keywords:\t" + dshash['tags'] + "\n"
    puts " - polling interval:\t" + ds_pollint_m.to_s + "m\n"
    puts " - multi instance:\t" + dshash['multi'] + "\n"
    puts " - datapoints:\t\t" + dshash['datapoints'].size.to_s + "\n"
    puts " - datapoint alerts:\t" + dp_alert_count.to_s + "\n"
    puts " - graphs:\t\t" + dshash['graphs'].size.to_s + "\n"
    puts " - overview graphs:\t" + dshash['ographs'].size.to_s + "\n"
    puts "=====================\n"
  end

  ## test_dsname - run tests on datasource name & displayname
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_dsname(dsobj)
    errors = []
    dshash = get_dshash(dsobj)

    # does the name contain whitespace?
    if ( dshash['name'] =~ /\s+/ )
      # yes -- record an error
      errors.push("datasource name contains whitespace")
    end

    # does the name end in a trailing dash?
    if ( dshash['name'] =~ /\-$/ )
      # yes -- record an error
      errors.push("datasource name has trailing dash")
    end

    # does the display name end in a trailing dash?
    if ( dshash['dname'] =~ /\-$/ )
      # yes -- record an error
      errors.push("datasource display name has trailing dash")
    end

    return(errors)
  end

  ## test_dsdesc - run tests on datasource description
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_dsdesc(dsobj)
    error  = nil
    dshash = get_dshash(dsobj)

    # is the description size less than 10 characters in length?
    if ( dshash['desc'].to_s.size < 10)
      # yes -- record an error
      error = "datasource description is empty or sparse"
    end

    return(error)
  end


  ## test_dstags - run tests on datasource tags
  ##   @param object dsobj - datasource object
  ##   @return array errors
  
  def test_dstags(dsobj)
    error = nil
    dshash = get_dshash(dsobj)

    # Are there one or more tags?
    if ( dshash['tags'].split(',').size < 3)
      # yes -- record an error
      error = "datasource search keywords are sparse"
    end
  end


  ## test_datapoint_desc - run tests on datapoint descriptions
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_datapoint_desc(dsobj)
    errors = []
    dshash = get_dshash(dsobj)

    # iterate over each datapoint
    dshash['datapoints'] .each { |datapoint|
      dphash = get_dphash(datapoint)

      # is the description size less than 10 characters in length?
      if ( dphash['desc'].to_s.size < 10)
        # yes -- record an error
        errors.push("datapoint \"" + dphash['name'] + "\" description is empty or sparse")
      end

    }

    return(errors)
  end

  ## test_datapoint_alert - run tests on datapoint alerts
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_datapoint_alert(dsobj)
    errors = []
    dshash = get_dshash(dsobj)

    tokens = [
      '##HOST##',
      '##VALUE##',
      '##DURATION##',
      '##START##',
    ]

    # iterate over each datapoint
    dshash['datapoints'].each { |datapoint|
      dphash   = get_dphash(datapoint)

      # is there a datapoint alert trigger set, but no custom alert message
      if ( dphash['alert_trigger'].size > 0 ) && ( dphash['alert_msgbody'].size == 0 )
        # yes -- record an error
        errors.push("datapoint \"" + dphash['name'] +
                    "\" has an alert threshold but no message")
      end

      # is there a custom alert message on this datapoint?
      if (  dphash['alert_msgbody'].size > 0 )
        tokens.each { |token|

          # is this token in the datasource definition?
          if( ! dphash['alert_msgbody'].include? token )
            # no -- record an error
            errors.push("custom alert message on \"" + dphash['name'] +
                        "\" datapoint doesn't include token " + token )
          end

        }
      end

    }

    return(errors)
  end

  ## test_datapoint_usage - run tests to determine where datapoints are used
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_datapoint_usage(dsobj)
    errors = []
    datapoint_ok = []
    dshash = get_dshash(dsobj)

    puts "Datapoints:\n"

    # iterate over each datapoint
    dshash['datapoints'].each { |datapoint|
      dphash   = get_dphash(datapoint)

      # is this a complex datapoint?
      if (dphash['ppmethod'] == 'expression')
        # yes -- set the type
        dp_type = 'complex datapoint'
      else
        # no -- set type to normal
        dp_type = 'normal datapoint'
      end

      # is an alert trigger set?
      if (dphash['alert_trigger'].size > 0)
        # yes -- add to report
        puts ' - ' + dp_type + ' "' + dphash['name'] + '" has alert threshold set'
        datapoint_ok.push(dphash['name'])
        next
      else
        # no. next check to see if this datapoint used in building any complex datapoints

        # again iterate over each of the datapoints (so we can look at complex ones)
        dshash['datapoints'].each { |test_datapoint|
          test_dphash   = get_dphash(test_datapoint)

          # is this a complex datapoint, and if so does the formula contain this datapoint?
          if (test_dphash['ppmethod'] == 'expression') && (test_dphash['ppparam'].include? dphash['name'])
            # yes -- add to report
            puts ' - ' + dp_type + ' "' + dphash['name'] + '" used in complex datapoint "' + test_dphash['name'] + '"'
            datapoint_ok.push(dphash['name'])
            break
          end

        }

        # next iterate through each normal graph definition
        dshash['graphs'].each { |test_graph|
          test_graphhash = get_graphhash(test_graph)

          # iterate through each graph datapoint
          test_graphhash['datapoints'].each { |test_gdps|

            test_gdp = get_graphdphash(test_gdps)

            # is this datapoint the one we're testing?
            if ( test_gdp['dpname'] == dphash['name'])
              # yes -- add to the report
              puts ' - ' + dp_type + ' "' + dphash['name'] + '" used in graph "' + test_graphhash['name'] + '" datapoint'
              datapoint_ok.push(dphash['name'])
              break
            end

          }
        }

        # next iterate through each overview graph definition
        dshash['ographs'].each { |test_ograph|
          test_ographhash = get_ographhash(test_ograph)

          # iterate through each graph datapoint
          test_ographhash['datapoints'].each { |test_ogdps|

            test_ogdp = get_graphdphash(test_ogdps)

            # is this datapoint the one we're testing?
            if ( test_ogdp['dpname'] == dphash['name'])
              # yes -- add to the report
              puts ' - ' + dp_type + ' "' + dphash['name'] + '" used in overview graph "' + test_ographhash['name'] + '" datapoint'
              datapoint_ok.push(dphash['name'])
              break
            end

          }
        }

        # did this datapoint check out in the above tests?
        if (! datapoint_ok.include?  dphash['name'] )
          # no -- lets record the error
          errors.push("datapoint \"" + dphash['name'] +
                      "\" appears to be unused")
        end
      end

    }

    puts "=====================\n"
    return(errors)
  end

  ## test_graphs - run tests to check graph styling
  ##   @param object dsobj - datasource object
  ##   @return array errors

  def test_graphs(dsobj)
    errors = []
    dshash = get_dshash(dsobj)

    graph_types = Hash[
      'graphs'   => 'Graphs',
      'ographs'  => 'Overview Graphs',
    ]

    # iterate over the two graph types
    graph_types.keys.each { |type|

      # init/reinit the display hash
      display = {}

      puts graph_types[type] + ":\n"

      # iterate over each graph definition
      dshash[type].each { |test_graph|
        graphhash = get_graphhash(test_graph)

        puts ' - "' + graphhash['name'] + '" at display priority ' + graphhash['display']

        # does the y-axis label contain capital letters?
        case graphhash['yaxis']
        when /^IOPS$/    # We'll allow capital letters for IOPS
          break
        when /[A-Z]+/  # does the y-axis label contain capital letters?
          # yes -- record an error
          errors.push("#{type} #{graphhash['name']} has uppercase letters in the y-axis definition (#{graphhash['yaxis']})")
        else
          next
        end

        # has this graph priority already been used?
        if ( display[graphhash['display']] )
          # yes -- record an error
          errors.push(type + ' "' + graphhash['name'] +
                      '" is assigned the same display priority (' +
                      graphhash['display'] + ') as "' +
                      display[graphhash['display']] + '"')
        else
          # no -- store this priority in a hash for further testing
          display[graphhash['display']] =  graphhash['name']
        end

      }

      puts "=====================\n"
    }

    return(errors)
  end
end

##
## main program
##

ds_test = DS_Test.new
ds_obj  = ds_test.get_datasource
ds_test.print_summary(ds_obj)

# init the errors array
errors = Array.new

# define tests to execute
tests = [
  'test_dsname',
  'test_dsdesc',
  'test_dstags',
  'test_datapoint_desc',
  'test_datapoint_alert',
  'test_datapoint_usage',
  'test_graphs'
]

# iterate through each of the defined tests
tests.each { |test|
  # this results in a 2D array of error objects
  errors.push(ds_test.send(test, ds_obj))
}

# display errors
puts "Proposed Fixes:\n"

# iterate through the 2D error report array by flattening to a 1D array
errors.flatten.each { |error|
  # is there an error defined?
  if (error)
    # print to screen
    puts " * " + error.to_s
  end

}
