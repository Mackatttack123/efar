

//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
// TODO: When an emergecy is clicked on....open the efar chat
//       show the info card...and show the address and efars
//       on the map.
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////



var geocoder;
var map;
var address = "cape town, south africa";

function initAutocomplete(){
  // Create the autocomplete object, restricting the search to geographical
  // location types.
  autocomplete = new google.maps.places.Autocomplete(
    (document.getElementById("map-input")),
    {types: ['geocode']
  });

  // When the user selects an address from the dropdown, populate the address
  // fields in the form.
  autocomplete.addListener('place_changed', function(){});

  geocoder = new google.maps.Geocoder();
  var latlng = new google.maps.LatLng(-33.1, 20.5);
  var myOptions = {
    zoom: 7,
    center: latlng,
    mapTypeControl: true,
    mapTypeControlOptions: {
      style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
    },
    navigationControl: true,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  map = new google.maps.Map(document.getElementById("map"), myOptions);
  setEFARLocations();
  //code before the pause
  setTimeout(function(){
      setUpMap();
  }, 7000);
}

var efar_array = [];
function setEFARLocations(){
  firebase.database().ref("/tokens/").on('value', function(tokens_snapshot) {
    efar_array = [];
    tokens_snapshot.forEach(function(childSnapshot) {

      var latitude = childSnapshot.child("latitude").val();
      var longitude = childSnapshot.child("longitude").val();
      
      efar = {latitude: latitude, longitude: longitude};
      efar_array.push(efar);
    });
  });
}

function addEFARs(){
  for (var i = efar_array.length - 1; i >= 0; i--) {
    efar_maker = new google.maps.LatLng(efar_array[i].latitude, efar_array[i].longitude);
    addEFARMarker(efar_maker);
  }
}

function addEFARMarker(location) {
    var icon = {
        url: "logo.svg", // url
        scaledSize: new google.maps.Size(40, 40), // scaled size
    };
    marker = new google.maps.Marker({
        position: location,
        map: map,
        icon: icon,
        title: "EFAR"
    });
}

function goToAddress(address){
  if (geocoder) {
    geocoder.geocode({
      'address': address
    }, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (status != google.maps.GeocoderStatus.ZERO_RESULTS) {
          var latitude = results[0].geometry.location.lat();
          var longitude = results[0].geometry.location.lng();

          var infowindow = new google.maps.InfoWindow({
            content: '<b>' + address + '</b>',
            size: new google.maps.Size(150, 50)
          });

          var latlng = new google.maps.LatLng(latitude, longitude);

          var mapOptions = {
            zoom: 14,
            center: latlng,
            mapTypeControlOptions: {
              style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
            },
            navigationControl: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP
          }

          map = new google.maps.Map(document.getElementById('map'), mapOptions);

          var latlng = new google.maps.LatLng(latitude, longitude);
          map.setCenter(latlng);

          var icon = {
              url: "emergency.svg", // url
              scaledSize: new google.maps.Size(50, 50), // scaled size
          };
          var marker = new google.maps.Marker({
            map: map,
            position: latlng,
            title: address,
            icon: icon
          });
          // Add circle overlay and bind to marker
          var circle = new google.maps.Circle({
            map: map,
            radius: 2000,    // 2 km
            strokeColor: '#FF0000',
            strokeOpacity: 0.8,
            strokeWeight: 2,
            fillColor: '#FF0000',
            fillOpacity: 0.15
          });
          circle.bindTo('center', marker, 'position');

          addEFARs();

          google.maps.event.addListener(marker, 'click', function() {
            infowindow.open(map, marker);
          });

        } else {
          alert("No results found");
        }
      } else {
        alert("Finding the loaction was not successful for the following reason: " + status);
      }
    });
  }
}

function setUpMap(){
  geocoder = new google.maps.Geocoder();
  var latlng = new google.maps.LatLng(-33.1, 20.5);
  var mapOptions = {
    zoom: 7,
    center: latlng,
    mapTypeControl: true,
    mapTypeControlOptions: {
      style: google.maps.MapTypeControlStyle.DROPDOWN_MENU
    },
    navigationControl: true,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  map = new google.maps.Map(document.getElementById('map'), mapOptions);

  // Add circle overlay and bind to marker
  var circle = new google.maps.Circle({
    map: map,
    radius: 2000,    // 2 km
    strokeColor: '#FF0000',
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#FF0000',
    fillOpacity: 0.15
  });
  addEFARs();
}