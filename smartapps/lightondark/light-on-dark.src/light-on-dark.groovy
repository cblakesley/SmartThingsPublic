/**
 *  Light On Dark
 *
 *  Copyright 2016 Christopher Blakesley
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
 
import java.text.SimpleDateFormat
 
definition(
    name: "Light On Dark",
    namespace: "LightOnDark",
    author: "Christopher Blakesley",
    description: "Turns on switches if it is dark",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Devices") {
        input "devSwitch", "capability.switch", required: true, title: "Select switch device?"
        input "devIllum", "capability.illuminanceMeasurement", required: true, title: "Select light sensor"
    }
    section("Running Time") {
        input(name: "tOn", type: "time", required: true, title: "Start Time")
        input(name: "tOff", type: "time", required: true, title: "Stop Time")
        input(name: "iRand", type: "number", required: true, title: "Random time offset +/- mins")
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(devIllum, "illuminance", hndlIllum)
    schedule("2015-01-01T00:30:00.000-0000", hndlUpdateTimes)
    hndlUpdateTimes()
    hndlTimeOn()
}

def hndlUpdateTimes() {
	Random rand = new Random(now())
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

	Date dOn = f.parse(tOn)
    long lRand = dOn.getTime() + (long)(2 * iRand * (rand.nextInt(60000) - 30000))
    Date dRand = new Date(lRand)
	schedule(dRand, hndlTimeOn)
    log.debug "Scheduled On at $dRand"

	Date dOff = f.parse(tOff)
    lRand = dOff.getTime() + (long)(2 * iRand * (rand.nextInt(60000) - 30000))
    dRand = new Date(lRand)
    schedule(dRand, hndlTimeOff)
    log.debug "Scheduled Off at $dRand"
}


def hndlIllum(evt) {
    log.debug "Handling evt.integerValue = $evt.integerValue"
    if (state.on != 1 && state.bTimerOn != 0 && evt.integerValue < 200) {
    	devSwitch.on()
        state.on = 1
	  	log.debug "Switched ON"
    }
    else if (state.on != 0 && (state.bTimerOn != 1 || evt.integerValue > 400)) {
    	devSwitch.off()
        state.on = 0
	  	log.debug "Switched OFF"
    }
}



def hndlTimeOn() {
    log.debug "Turning Function ON at time"
    state.bTimerOn = 1
}
    
def hndlTimeOff() {
    log.debug "Turning Function OFF at time"
    state.bTimerOn = 0
}
