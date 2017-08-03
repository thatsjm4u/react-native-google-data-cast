import { NativeModules } from 'react-native';

const { GpsModule } = NativeModules;

export default {
  getLocationUpdates: function () {
	GpsModule.getLocationUpdates();
  },
  getLastKnowLocation: function () {
	GpsModule.getLastKnowLocation();
  },
  getCurrentSingleLocation: function () {
	return GpsModule.getCurrentSingleLocation();
  },
  stopLocationUpdate: function () {
	return GpsModule.stopLocationUpdate();
  },
  isGpsEnabled: function () {
	return GpsModule.isGpsEnabled();
  },
  DEVENT_FOUND_LOCATION: GpsModule.EVENT_FOUND_LOCATION,
  EVENT_TIME_OUT: GpsModule.EVENT_TIME_OUT
};