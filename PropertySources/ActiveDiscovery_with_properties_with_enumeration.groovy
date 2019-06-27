/*******************************************************************************
 * © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/
import com.santaba.agent.groovyapi.snmp.Snmp
baseOID = ".1.3.6.1.4.1.15450.2.1.7.1.1" //parent OID that contains the wildvalue, wildalias, and properties
aliasOID = "49" //leaf OID that contains the wildalias, we'll walk this to get the wildvalue and wildalias
/*******************************************************************************
 * This map contains two elements per entry:
 *     The first is the property name as it will appear in LM properties
 *     The second is a mapping of enumerated values that can be polled from the MIB
 *       mapped to the corresponding meaning.
 * Make sure the keys in the second element map are strings.
 * Also make sure that properties that do not have an enumeration have a null map
 * as the second element.
 ******************************************************************************/
propstoget = [
  4: ["mGuard.VPN.TunType",          ["1":"tunnel","2":"transport","3":"modecfg"]   ],
  5: ["mGuard.VPN.TunLocal",         [:]                                            ],
  6: ["mGuard.VPN.TunRemote",        [:]                                            ],
  7: ["mGuard.VPN.TunVirtIP",        [:]                                            ],
  11:["mGuard.VPN.TunRemote1to1Nat", [:]                                            ],
  12:["mGuard.VPN.TunProto",         ["1":"icmp","6":"tcp","17":"udp","256":"all"]  ],
  13:["mGuard.VPN.TunLocalPort",     [:]                                            ],
  14:["mGuard.VPN.TunRemotePort",    [:]                                            ],
  15:["mGuard.VPN.TunDummyRoute",    ["1":"yes","2":"no"]                           ],
  52:["mGuard.VPN.TunLocalMasq",     [:]                                            ],
  53:["mGuard.VPN.TunRemoteMasq",    [:]                                            ],
  54:["mGuard.VPN.TunLocalNat",      ["1":"none","2":"use1to1nat","3":"masq"]       ],
  55:["mGuard.VPN.TunRemoteNat",     ["1":"none","2":"use1to1nat","3":"masq"]       ],
]
//You shouldn't have to modify anything after this line
hostname = hostProps.get('system.hostname')
Snmp.walkAsMap(hostname,baseOID + "." + aliasOID,null).each { instance -> //get the wildvalue and wildalias of each instance
  props = []
  propstoget.each {key, val ->
    propresult = URLEncoder.encode(Snmp.get(hostname, baseOID + "." + key + "." + instance.key)) //poll for the value of the property
    if(val[1].containsKey(propresult)) { propresult = val[1][propresult] }//if the polled value has a corresponding enumerated value
    props << val[0] + "=" + propresult //can't append like a string, append as a list and join later.
  }
  println("${instance.key}##${instance.value}##${instance.value}####${props.join('&')}")
}
return(0)
