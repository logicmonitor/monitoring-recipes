/*******************************************************************************
 * © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 * This script can be modified for use in a Property Source discovering multiple
 * properties. In addition to discovering the properties, if there are any
 * enumerations defined in the MIB, they can be added to the propstoget map
 * below and the script will replace the polled value with the enumerated word.
 * For example, instead of saving a "2" for .1.3.6.1.4.1.15450.2.2.5.11.0, the
 * word "ping(2)" would be stored in the property instead.
 * The included MGUARDB-MIB.txt file details the enumerations if you want to see
 * the source of the definitions shown.
 ******************************************************************************/
import com.santaba.agent.groovyapi.snmp.Snmp


/*******************************************************************************
 * This map contains two elements per entry:
 *     The first is the property name as it will appear in LM properties
 *     The second is a mapping of enumerated values that can be polled from the MIB
 *       mapped to the corresponding meaning.
 * Make sure the keys in the second element map are strings.
 * Also make sure that properties that do not have an enumeration have a null map
 * as the second element.
 ******************************************************************************/
 def propstoget = [
  "mGuard.VPN.DynDNSReg":               [".1.3.6.1.4.1.15450.2.1.4.1.1.0",   ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.DynDNSRegInterval":       [".1.3.6.1.4.1.15450.2.1.4.1.2.0",   [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSRegServer":         [".1.3.6.1.4.1.15450.2.1.4.1.3.0",   [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSRegLogin":          [".1.3.6.1.4.1.15450.2.1.4.1.4.0",   [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSRegPasswd":         [".1.3.6.1.4.1.15450.2.1.4.1.5.0",   [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSRegHostname":       [".1.3.6.1.4.1.15450.2.1.4.1.7.0",   [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSRegProviderNew":    [".1.3.6.1.4.1.15450.2.1.4.1.9.0",   ["1":"dyndns-compatible(1)","2":"dyndns(2)","3":"no-ip(3)","4":"freedns(4)","5":"easydns(5)","6":"dnsexit(6)","7":"dynu(7)"] ],
  "mGuard.VPN.DynDNSRegPort":           [".1.3.6.1.4.1.15450.2.1.4.1.10.0",  [:]                                                                                                                          ],
  "mGuard.VPN.DynDNSCheckDo":           [".1.3.6.1.4.1.15450.2.1.4.2.1.0",   ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.DynDNSCheckRefresh":      [".1.3.6.1.4.1.15450.2.1.4.2.2.0",   [:]                                                                                                                          ],
  "mGuard.VPN.RequireUniqueIDs":        [".1.3.6.1.4.1.15450.2.1.6.1.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.NatTraversal":            [".1.3.6.1.4.1.15450.2.1.6.2.0",     ["1":"on(1)","2":"off(2)"]                                                                                                   ],
  "mGuard.VPN.NatTPortfloating":        [".1.3.6.1.4.1.15450.2.1.6.3.0",     ["1":"on(1)","2":"off(2)"]                                                                                                   ],
  "mGuard.VPN.NatTKeepAliveInterval":   [".1.3.6.1.4.1.15450.2.1.6.4.0",     [:]                                                                                                                          ],
  "mGuard.VPN.NatTKeepAliveForce":      [".1.3.6.1.4.1.15450.2.1.6.5.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.IkeLog":                  [".1.3.6.1.4.1.15450.2.1.6.6.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.HideTos":                 [".1.3.6.1.4.1.15450.2.1.6.7.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.mtu":                     [".1.3.6.1.4.1.15450.2.1.6.8.0",     [:]                                                                                                                          ],
  "mGuard.VPN.NoCertReqSend":           [".1.3.6.1.4.1.15450.2.1.6.10.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.HubAndSpoke":             [".1.3.6.1.4.1.15450.2.1.6.11.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.IPsecTcpMss":             [".1.3.6.1.4.1.15450.2.1.6.13.0",    [:]                                                                                                                          ],
  "mGuard.VPN.Switch1Type":             [".1.3.6.1.4.1.15450.2.1.6.15.0",    ["1":"button(1)","2":"switch(2)"]                                                                                            ],
  "mGuard.VPN.RoutingType":             [".1.3.6.1.4.1.15450.2.1.6.16.0",    ["1":"all-interfaces(1)","2":"intern-only(2)"]                                                                               ],
  "mGuard.VPN.IPtunEnable":             [".1.3.6.1.4.1.15450.2.1.6.17.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.IPtunListenport":         [".1.3.6.1.4.1.15450.2.1.6.18.0",    [:]                                                                                                                          ],
  "mGuard.VPN.IPtunPool":               [".1.3.6.1.4.1.15450.2.1.6.19.0",    [:]                                                                                                                          ],
  "mGuard.VPN.LogPersistEnable":        [".1.3.6.1.4.1.15450.2.1.6.20.1.0",  ["2":"no(2)","3":"yes(3)"]                                                                                                   ],
  "mGuard.VPN.LogPersistFailuresOnly":  [".1.3.6.1.4.1.15450.2.1.6.20.2.0",  ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.LogPersistLeastFree":     [".1.3.6.1.4.1.15450.2.1.6.20.3.0",  [:]                                                                                                                          ],
  "mGuard.VPN.LogPersistMaxFPS":        [".1.3.6.1.4.1.15450.2.1.6.20.5.0",  [:]                                                                                                                          ],
  "mGuard.VPN.Switch2Type":             [".1.3.6.1.4.1.15450.2.1.6.32.0",    ["1":"button(1)","2":"switch(2)"]                                                                                            ],
  "mGuard.VPN.Switch3Type":             [".1.3.6.1.4.1.15450.2.1.6.33.0",    ["1":"button(1)","2":"switch(2)"]                                                                                            ],
  "mGuard.VPN.TCPEncapEnable":          [".1.3.6.1.4.1.15450.2.1.6.34.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.TCPEncapListenport":      [".1.3.6.1.4.1.15450.2.1.6.35.0",    [:]                                                                                                                          ],
  "mGuard.VPN.IkeFragmentation":        [".1.3.6.1.4.1.15450.2.1.6.36.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.AlwaysSendNATOA":         [".1.3.6.1.4.1.15450.2.1.6.37.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.VPN.AESCBox":                 [".1.3.6.1.4.1.15450.2.1.6.38.0",    [:]                                                                                                                          ],
  "mGuard.FW.INLogDefault":             [".1.3.6.1.4.1.15450.2.2.1.2.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.INuuid":                   [".1.3.6.1.4.1.15450.2.2.1.3.0",     [:]                                                                                                                          ],
  "mGuard.FW.INGlobal":                 [".1.3.6.1.4.1.15450.2.2.1.4.0",     ["1":"accept(1)","2":"drop(2)","3":"rules(3)","4":"ping(4)"]                                                                 ],
  "mGuard.FW.OUTLogDefault":            [".1.3.6.1.4.1.15450.2.2.2.2.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.OUTuuid":                  [".1.3.6.1.4.1.15450.2.2.2.3.0",     [:]                                                                                                                          ],
  "mGuard.FW.OUTGlobal":                [".1.3.6.1.4.1.15450.2.2.2.4.0",     ["1":"accept(1)","2":"drop(2)","3":"rules(3)","4":"ping(4)"]                                                                 ],
  "mGuard.FW.FWDuuid":                  [".1.3.6.1.4.1.15450.2.2.3.2.0",     [:]                                                                                                                          ],
  "mGuard.FW.IPConntrackMax":           [".1.3.6.1.4.1.15450.2.2.5.1.0",     [:]                                                                                                                          ],
  "mGuard.FW.IPSynfloodLimitInt":       [".1.3.6.1.4.1.15450.2.2.5.2.0",     [:]                                                                                                                          ],
  "mGuard.FW.IPSynfloodLimitExt":       [".1.3.6.1.4.1.15450.2.2.5.3.0",     [:]                                                                                                                          ],
  "mGuard.FW.ICMPLimitInt":             [".1.3.6.1.4.1.15450.2.2.5.4.0",     [:]                                                                                                                          ],
  "mGuard.FW.ICMPLimitExt":             [".1.3.6.1.4.1.15450.2.2.5.5.0",     [:]                                                                                                                          ],
  "mGuard.FW.EnableConntrackFTP":       [".1.3.6.1.4.1.15450.2.2.5.6.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackIRC":             [".1.3.6.1.4.1.15450.2.2.5.7.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackPPTP":            [".1.3.6.1.4.1.15450.2.2.5.8.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ARPLimitInt":              [".1.3.6.1.4.1.15450.2.2.5.9.0",     [:]                                                                                                                          ],
  "mGuard.FW.ARPLimitExt":              [".1.3.6.1.4.1.15450.2.2.5.10.0",    [:]                                                                                                                          ],
  "mGuard.FW.ICMPPolicy":               [".1.3.6.1.4.1.15450.2.2.5.11.0",    ["1":"drop(1)","2":"ping(2)","3":"all(3)"]                                                                                   ],
  "mGuard.FW.ConntrackH323":            [".1.3.6.1.4.1.15450.2.2.5.12.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.IpUncleanMatch":           [".1.3.6.1.4.1.15450.2.2.5.13.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ackTcpTimeoutEstab":       [".1.3.6.1.4.1.15450.2.2.5.14.0",    [:]                                                                                                                          ],
  "mGuard.FW.ConntrackSIP":             [".1.3.6.1.4.1.15450.2.2.5.15.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackReqTcpSyn":       [".1.3.6.1.4.1.15450.2.2.5.16.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ICMPLengthMax":            [".1.3.6.1.4.1.15450.2.2.5.17.0",    [:]                                                                                                                          ],
  "mGuard.FW.ackTcpTimeoutCWait":       [".1.3.6.1.4.1.15450.2.2.5.18.0",    [:]                                                                                                                          ],
  "mGuard.FW.ICMPPolicy2":              [".1.3.6.1.4.1.15450.2.2.5.19.0",    ["1":"drop(1)","2":"ping(2)","3":"all(3)"]                                                                                   ],
  "mGuard.FW.ackTcpNoFlagsEst":         [".1.3.6.1.4.1.15450.2.2.5.20.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackFlush":           [".1.3.6.1.4.1.15450.2.2.5.21.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ICMPPolicyDmz0":           [".1.3.6.1.4.1.15450.2.2.5.22.0",    ["1":"drop(1)","2":"ping(2)","3":"all(3)"]                                                                                   ],
  "mGuard.FW.ConntrackOPC":             [".1.3.6.1.4.1.15450.2.2.5.23.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackOPCSanity":       [".1.3.6.1.4.1.15450.2.2.5.24.0",    ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.ConntrackOPCTimeout":      [".1.3.6.1.4.1.15450.2.2.5.25.0",    [:]                                                                                                                          ],
  "mGuard.FW.UsrFWEnabled":             [".1.3.6.1.4.1.15450.2.2.7.1.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.UsrFWGroupAuthEnabled":    [".1.3.6.1.4.1.15450.2.2.7.5.0",     ["1":"yes(1)","2":"no(2)"]                                                                                                   ],
  "mGuard.FW.WDINuuid":                 [".1.3.6.1.4.1.15450.2.2.10.3.0",    [:]                                                                                                                          ],

]
//You shouldn't have to modify anything after this line
hostname = hostProps.get("system.hostname")
propstoget.each {key, val -> //Loop through the properties to fetch
  propresult = URLEncoder.encode(Snmp.get(hostname, val[0])) //fetch the value and sanitize it
  if (val[1].containsKey(propresult)) {propresult = val[1][propresult]} //if the fetched value has an enumeration, replace it with the word
  println(key + "=" + propresult) //output the property name and value
}
return 0
