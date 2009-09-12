package org.penemunxt.pcserver.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class NXTDataExchanger extends Thread {
	boolean Active;
	boolean WriteBeforeRead;
	int WaitMilliseconds[];

	INXTCommunicationDataFactories DataFactories;

	DataOutputStream DataOut;
	DataInputStream DataIn;

	NXTCommunicationQueue DataRetrievedQueue;
	NXTCommunicationQueue DataSendQueue;

	public NXTCommunicationQueue getDataRetrievedQueue() {
		return DataRetrievedQueue;
	}

	public void setDataRetrievedQueue(NXTCommunicationQueue dataRetrievedQueue) {
		DataRetrievedQueue = dataRetrievedQueue;
	}

	public NXTCommunicationQueue getDataSendQueue() {
		return DataSendQueue;
	}

	public void setDataSendQueue(NXTCommunicationQueue dataSendQueue) {
		DataSendQueue = dataSendQueue;
	}

	public int[] getWaitMilliseconds() {
		return WaitMilliseconds;
	}

	public NXTDataExchanger(boolean WriteBeforeRead, DataOutputStream DataOut,
			DataInputStream DataIn, NXTCommunicationQueue DataRetrievedQueue,
			NXTCommunicationQueue DataSendQueue,
			INXTCommunicationDataFactories DataFactories) {
		super();
		this.WriteBeforeRead = WriteBeforeRead;
		this.DataOut = DataOut;
		this.DataIn = DataIn;
		this.DataRetrievedQueue = DataRetrievedQueue;
		this.DataSendQueue = DataSendQueue;
		this.Active = true;
		this.DataFactories = DataFactories;

		this.WaitMilliseconds = new int[] { 0, 100, 250, 500, 1000, 1500, 2000,
				2500, 3000, 5000, 7000, 10000 };
	}

	public NXTDataExchanger(boolean WriteBeforeRead, DataOutputStream DataOut,
			DataInputStream DataIn, NXTCommunicationQueue DataRetrievedQueue,
			NXTCommunicationQueue DataSendQueue,
			INXTCommunicationDataFactories DataFactories,
			int WaitMilliseconds[]) {

		this(WriteBeforeRead, DataOut, DataIn, DataRetrievedQueue,
				DataSendQueue, DataFactories);

		this.WaitMilliseconds = WaitMilliseconds;
	}

	public void End() {
		this.Active = false;
	}

	public void run() {
		int WaitPos = -1;
		boolean ResetWaitPos;

		while (this.Active) {
			ResetWaitPos = false;

			if (this.WriteBeforeRead) {
				if (this.Write()) {
					ResetWaitPos = true;
				}
				if (this.Read()) {
					ResetWaitPos = true;
				}
			} else {
				if (this.Read()) {
					ResetWaitPos = true;
				}
				if (this.Write()) {
					ResetWaitPos = true;
				}
			}

			if (ResetWaitPos) {
				WaitPos = -1;
			}
			if (WaitPos < (WaitMilliseconds.length - 1)) {
				WaitPos += 1;
			}

			if (WaitMilliseconds[WaitPos] == 0) {
				Thread.yield();
			} else {
				try {
					Thread.sleep(WaitMilliseconds[WaitPos]);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private boolean Write() {
		boolean RealData;
		INXTCommunicationData DataItemOut = (INXTCommunicationData) DataFactories
				.getDataOutFactory().getEmptyInstance();

		// Write
		if (this.getDataSendQueue().getQueueSize() > 0) {
			DataItemOut = this.getDataSendQueue().getAndDeleteNextItem();
			RealData = true;
		} else {
			RealData = false;
		}

		try {
			DataItemOut.WriteData(DataOut);
			this.DataOut.flush();
		} catch (IOException ioe) {
			// System.err.println("Data write error");
		}

		DataItemOut = null;

		return RealData;
	}

	private boolean Read() {
		boolean RealData;
		INXTCommunicationData DataItemIn = (INXTCommunicationData) DataFactories
				.getDataInFactory().getEmptyInstance();

		// Read
		try {
			DataItemIn.ReadData(DataIn);
		} catch (IOException ioe) {
			// System.err.println("Data read error");
		}

		if (DataItemIn != null
				&& DataItemIn.getDataStatus() != NXTCommunicationData.DATA_STATUS_EMPTY) {
			this.DataRetrievedQueue.add(DataItemIn);
			RealData = true;
		} else {
			RealData = false;
		}

		DataItemIn = null;

		return RealData;
	}
}
