var itemsCompletedNotFormatted = [];
var itemsCompletedFormatted = [];
var completed_ready_to_download = false;
var loading_text;
var completed_info_text;
var completed_download_button;

function setup(){
    loading_text = select("#loading_text");
    completed_info_text = select("#completed_info_text");
    completed_download_button = select("#completed_download_button");
    checkGrapple();
}

function checkGrapple(){
    firebase.database().ref("/dispatchers/" + user_id).once('value', function(snapshot) {
        if(snapshot.child("grapple").exists()){
            if(!(snapshot.child("grapple").val().trim() === "hook")){
                window.location = 'index.html';
            }
        }
    });
}

function getFirebaseRawCompleted(){
    firebase.database().ref("completed").on('value', function(snap){
        snap.forEach(function(childNode){
            checkGrapple();
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.open( "GET", "https://efarapp-a5f18.firebaseio.com/completed/" + childNode.key + ".json?auth=n8g00RPXVM2TbtZXpJ9RDblgRCi866SHu6WGTPYK&print=pretty", false ); // false for synchronous request
            xmlHttp.send( null );
            console.log(xmlHttp.responseText);
            itemsCompletedNotFormatted.push(JSON.parse(xmlHttp.responseText));
        });
        completed_ready_to_download = true;
        loading_text.hide();
        completed_info_text.attribute("style", "visibility: block");
        completed_download_button.attribute("style", "visibility: block");
        select("#json_links").attribute("style", "visibility: block;");
    });
}

var completedEmergencyHeaders = {
    //basic details
    address: "Address".replace(/,/g, ''),
    reference_number: "EMS_Reference_Number",
    call_received_time: "EMS_Call_Received_Time",
    creation_date: "Date/Time_EFAR_call_started",
    ended_date: "Date/Time_EFAR_call_ended",
    emergency_made_by_dispatcher : "Emergency Made By Dispatcher",
    latitude: "Latitude",
    longitude: "Longitude",
    other_info: "Information_given_to_EFARs",
    phone_number: "Phone_Number_Given_to_EFARs",
    responding_efar: "IDs_of_EFARs_who_responded",
    // patient care report form
    pcr_comments: "pcr -> comments",

    pcr_community: "pcr -> Incident_Details -> community",
    pcr_ems_or_ambulance_time: "pcr -> Incident_Details -> Time_EMS/Ambulace_on_scene",
    pcr_incident_location: "pcr -> Incident Details -> Incident Location",

    pcr_Weapon_used: "pcr -> Injury_Details -> Weapon_Used",
    pcr_awake: "pcr -> Injury_Details -> Injuries_and_illness -> awake",
    pcr_bleeding: "pcr -> Injury_Details -> Injuries_and_illness -> bleeding",
    pcr_burns: "pcr -> Injury_Details -> Injuries_and_illness -> burns",
    pcr_choking: "pcr -> Injury_Details -> Injuries_and_illness -> choking",
    pcr_fracture: "pcr -> Injury_Details -> Injuries_and_illness -> fracture",
    pcr_unconscious: "pcr -> Injury_Details -> Injuries_and_illness -> unconscious",
    pcr_other: "pcr -> Injury_Details -> Injuries_and_illness -> other",

    pcr_abdominal_pain: "pcr -> Injury_Details -> medical_emergency -> abdominal pain",
    pcr_chest_pain: "pcr -> Injury_Details -> medical_emergency -> chest_pain",
    pcr_dehydrated: "pcr -> Injury_Details -> medical_emergency -> dehydrated",
    pcr_diabetes: "pcr -> Injury_Details -> medical_emergency -> diabetes",
    pcr_difficulty_breathing: "pcr -> Injury_Details -> medical_emergency -> difficulty_breathing",
    pcr_epilepsy: "pcr -> Injury_Details -> medical_emergency -> epilepsy",
    pcr_poisoning: "pcr -> Injury_Details -> medical_emergency -> poisoning",
    pcr_stroke: "pcr -> Injury_Details -> medical_emergency -> stroke",
    pcr_other: "pcr -> Injury_Details -> medical_emergency -> other",
    pcr_motor_vehicle_accident: "pcr -> Injury_Details -> Motor_Vehicle_Accident",

    pcr_age: "pcr -> patient age",
    pcr_gender: "pcr -> patient gender",

    pcr_apply_bandage: "pcr -> treatment_details -> care_provided -> apply_bandage",
    pcr_assist_patient_in_taking_own_medication: "pcr -> treatment_details -> care provided -> assist patient in taking own medication",
    pcr_cpr: "pcr -> treatment details -> care provided -> cpr",
    pcr_recovery_position: "pcr -> treatment details -> care provided -> recovery position",
    pcr_splinting: "pcr -> treatment details -> care provided -> splinting",
    pcr_stop_bleeding: "pcr -> treatment details -> care provided -> stop bleeding",
    pcr_other: "pcr -> treatment details -> care provided -> other",
      
    pcr_aed: "pcr -> treatment details -> equipment used -> aed",
    pcr_bandage: "pcr -> treatment details -> equipment used -> aed",
    pcr_gloves: "pcr -> treatment details -> equipment used -> gloves",
    pcr_three_sided_dressing: "pcr -> treatment details -> equipment used -> three sided dressing",
    pcr_other: "pcr -> treatment details -> equipment used -> other",

    pcr_hospital_taken_to: "pcr - > hospital -> hospital taken to",
    pcr_patient_taken_to_hospital: "pcr -> hospital -> patient taken to hospital",
    pcr_EFAR: "pcr -> hospital -> transported by EFAR",
    pcr_ambulace: "pcr -> hospital -> transported by ambulace",
    pcr_private_car: "pcr -> hospital -> transported by private car",
};

function getFirebaseCompletedData(){
   //make sure all fields exsist
    itemsCompletedNotFormatted.forEach((item) =>{
        if(item.address === undefined || item.address === ""){
            item.address = "N/A";
        }
        if(item.call_received_time === undefined || item.call_received_time === ""){
            item.call_received_time = "N/A";
        }
        if(item.emergency_made_by_dispatcher === undefined || item.emergency_made_by_dispatcher === ""){
            item.emergency_made_by_dispatcher = "N/A";
        }
        if(item.phone_number === undefined || item.phone_number === ""){
            item.phone_number = "N/A";
        }
        if(item.reference_number === undefined || item.reference_number === ""){
            item.reference_number = "N/A";
        }
        if(item.responding_efar === undefined || item.responding_efar === ""){
            item.responding_efar = "N/A";
        }
        if(item.other_info === undefined || item.other_info === ""){
            item.other_info = "N/A";
        }
        // format the data
        itemsCompletedFormatted.push({
            //basic details
            address : item.address.replace(/,/g, ''),
            reference_number: item.reference_number.replace(/,/g, ''),
            call_received_time : item.call_received_time.replace(/,/g, ''),
            creation_date : item.creation_date,
            ended_date: item.ended_date,
            emergency_made_by_dispatcher : item.emergency_made_by_dispatcher.replace(/,/g, ''),
            latitude: item.latitude,
            longitude: item.longitude,
            other_info: item.other_info.replace(/,/g, ''),
            phone_number: item.phone_number.replace(/,/g, ''),
            responding_efar: item.responding_efar.replace(/,/g, ''),
            // patient care report form
            pcr_comments: item.patient_care_report_form.comments.replace(/,/g, ''),

            pcr_community: item.patient_care_report_form.incident_details.community.replace(/,/g, ''),
            pcr_ems_or_ambulance_time: item.patient_care_report_form.incident_details.ems_or_ambulance_time,
            pcr_incident_location: item.patient_care_report_form.incident_details.incident_location.replace(/,/g, ''),

            pcr_Weapon_used: item.patient_care_report_form.injury_details.Weapon_Used,
            pcr_awake: item.patient_care_report_form.injury_details.injuries_and_illness.awake,
            pcr_bleeding: item.patient_care_report_form.injury_details.injuries_and_illness.bleeding,
            pcr_burns: item.patient_care_report_form.injury_details.injuries_and_illness.burns,
            pcr_choking: item.patient_care_report_form.injury_details.injuries_and_illness.choking,
            pcr_fracture: item.patient_care_report_form.injury_details.injuries_and_illness.fracture,
            pcr_unconscious: item.patient_care_report_form.injury_details.injuries_and_illness.unconscious,
            pcr_other: item.patient_care_report_form.injury_details.injuries_and_illness.other.replace(/,/g, ''),

            pcr_abdominal_pain: item.patient_care_report_form.injury_details.medical_emergency.abdominal_pain,
            pcr_chest_pain: item.patient_care_report_form.injury_details.medical_emergency.chest_pain,
            pcr_dehydrated: item.patient_care_report_form.injury_details.medical_emergency.dehydrated,
            pcr_diabetes: item.patient_care_report_form.injury_details.medical_emergency.diabetes,
            pcr_difficulty_breathing: item.patient_care_report_form.injury_details.medical_emergency.difficulty_breathing,
            pcr_epilepsy: item.patient_care_report_form.injury_details.medical_emergency.epilepsy,
            pcr_poisoning: item.patient_care_report_form.injury_details.medical_emergency.poisoning,
            pcr_stroke: item.patient_care_report_form.injury_details.medical_emergency.stroke,
            pcr_other: item.patient_care_report_form.injury_details.medical_emergency.other.replace(/,/g, ''),
            pcr_motor_vehicle_accident: item.patient_care_report_form.injury_details.Motor_Vehicle_Accident,

            pcr_age: item.patient_care_report_form.patient_details.age.replace(/,/g, ''),
            pcr_gender: item.patient_care_report_form.patient_details.gender,

            pcr_apply_bandage: item.patient_care_report_form.treatment_details.care_provided.apply_bandage,
            pcr_assist_patient_in_taking_own_medication: item.patient_care_report_form.treatment_details.care_provided.assist_patient_in_taking_own_medication,
            pcr_cpr: item.patient_care_report_form.treatment_details.care_provided.cpr,
            pcr_recovery_position: item.patient_care_report_form.treatment_details.care_provided.recovery_position,
            pcr_splinting: item.patient_care_report_form.treatment_details.care_provided.splinting,
            pcr_stop_bleeding: item.patient_care_report_form.treatment_details.care_provided.stop_bleeding,
            pcr_other: item.patient_care_report_form.treatment_details.care_provided.other.replace(/,/g, ''),
              
            pcr_aed: item.patient_care_report_form.treatment_details.equipment_used.aed,
            pcr_bandage: item.patient_care_report_form.treatment_details.equipment_used.bandage,
            pcr_gloves: item.patient_care_report_form.treatment_details.equipment_used.gloves,
            pcr_three_sided_dressing: item.patient_care_report_form.treatment_details.equipment_used.three_sided_dressing,
            pcr_other: item.patient_care_report_form.treatment_details.equipment_used.other.replace(/,/g, ''),

            pcr_hospital_taken_to: item.patient_care_report_form.treatment_details.hospital.hospital_taken_to.replace(/,/g, ''),
            pcr_patient_taken_to_hospital: item.patient_care_report_form.treatment_details.hospital.patient_taken_to_hospital,
            pcr_EFAR: item.patient_care_report_form.treatment_details.hospital.transport.EFAR,
            pcr_ambulace: item.patient_care_report_form.treatment_details.hospital.transport.ambulace,
            pcr_private_car: item.patient_care_report_form.treatment_details.hospital.transport.private_car,
        });
    });
    exportCSVFile(completedEmergencyHeaders, itemsCompletedFormatted, "completed_emergecies_data");
}

function getCompletedCsv(){
    if(completed_ready_to_download){
        itemsCompletedFormatted = [];
        getFirebaseCompletedData();
    }else{
        alert("not ready to download yet!");
    }
}

function convertToCSV(objArray) {
    var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
    var str = '';

    for (var i = 0; i < array.length; i++) {
        var line = '';
        for (var index in array[i]) {
            if (line != '') line += ','

            line += array[i][index];
        }

        str += line + '\r\n';
    }

    return str;
}

function exportCSVFile(headers, items, fileTitle) {
    if (headers) {
        items.unshift(headers);
    }

    // Convert Object to JSON
    var jsonObject = JSON.stringify(items);

    var csv = this.convertToCSV(jsonObject);

    var exportedFilenmae = fileTitle + '.csv' || 'export.csv';

    var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    if (navigator.msSaveBlob) { // IE 10+
        navigator.msSaveBlob(blob, exportedFilenmae);
    } else {
        var link = document.createElement("a");
        if (link.download !== undefined) { // feature detection
            // Browsers that support HTML5 download attribute
            var url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", exportedFilenmae);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
}



