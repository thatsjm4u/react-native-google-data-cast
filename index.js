import { NativeModules } from 'react-native';

const { GoogleCastData } = NativeModules;

export default {
  startScan: function (ApplicationID: ?string) {
	GoogleCastData.startScan(ApplicationID);
  },
  stopScan: function () {
	GoogleCastData.stopScan();
  },
  isConnected: function () {
	return GoogleCastData.isConnected();
  },
  getDevices: function () {
	return GoogleCastData.getDevices();
  },
  connectToDevice: function (deviceId: string) {
	GoogleCastData.connectToDevice(deviceId);
  },
  disconnect: function () {
	GoogleCastData.disconnect();
  },
  sendDataMessage: function (message: string, namespace: string) {
	GoogleCastData.sendDataMessage(message, namespace);
  },
  DEVICE_AVAILABLE: GoogleCastData.DEVICE_AVAILABLE,
  DEVICE_CONNECTED: GoogleCastData.DEVICE_CONNECTED,
  DEVICE_DISCONNECTED: GoogleCastData.DEVICE_DISCONNECTED,
  MEDIA_LOADED: GoogleCastData.MEDIA_LOADED,
  APP_FAILED: GoogleCastData.APP_FAILED,
  MSG_SEND_FAILED: GoogleCastData.MSG_SEND_FAILED,
  MSG_RECEIVED: GoogleCastData.MSG_RECEIVED,

};