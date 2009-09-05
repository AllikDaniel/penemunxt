import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.comm.RConsole;

public class NXTCommTest implements Runnable {
	public static void main(String[] args) {
		NXTCommTest NXTCT = new NXTCommTest();
		NXTCT.run();
	}

	@Override
	public void run() {
		//RConsole.openBluetooth(10000);

		LCD.drawInt(0, 1, 1);
		NXTCommunication NXTComm = new NXTCommunication();
		NXTComm.ConnectAndStartAll(NXTConnectionModes.Bluetooth);
		LCD.drawInt(1, 1, 1);
		NXTComm.DataSendQueue.add(new NXTCommunicationTestData(
				NXTCommunicationData.STATUS_NORMAL,
				NXTCommunicationData.DATA_NORMAL_DATA, 1, 5));
		NXTComm.DataSendQueue.add(new NXTCommunicationTestData(
				NXTCommunicationData.STATUS_NORMAL,
				NXTCommunicationData.DATA_NORMAL_DATA, 6, 789));
		NXTComm.DataSendQueue.add(new NXTCommunicationTestData(
				NXTCommunicationData.STATUS_NORMAL,
				NXTCommunicationData.DATA_NORMAL_DATA, 343, 534));
		while (!Button.ESCAPE.isPressed()) {
			LCD.clear();

			NXTCommunicationTestData DataItem = (NXTCommunicationTestData) NXTComm.DataRetrievedQueue
					.get(0);

			LCD.drawInt(NXTComm.DataRetrievedQueue.size(), 1, 1);
			if (NXTComm.DataRetrievedQueue.size() > 0) {
				LCD.drawInt(DataItem.MainStatus, 1, 3);
				LCD.drawInt(DataItem.DataStatus, 1, 4);
				LCD.drawInt(DataItem.Param1, 1, 5);
				LCD.drawInt(DataItem.Param2, 1, 6);
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}
}