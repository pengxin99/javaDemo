import java.io.BufferedReader;
import java.io.FileReader;


class FunctionUnit {
	private String Name;
	private boolean Busy;
	private String Op;
	private String Fi;
	private String Fj;
	private String Fk;
	private String Qj;
	private String Qk;
	boolean Rj;
	boolean Rk;

	public FunctionUnit(String name, boolean busy, String op, String fi, String fj, String fk, String qj, String qk,
			boolean rj, boolean rk) {
		super();
		Name = name;
		Busy = busy;
		Op = op;
		Fi = fi;
		Fj = fj;
		Fk = fk;
		Qj = qj;
		Qk = qk;
		Rj = rj;
		Rk = rk;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public boolean isBusy() {
		return Busy;
	}

	public void setBusy(boolean busy) {
		Busy = busy;
	}

	public String getOp() {
		return Op;
	}

	public void setOp(String op) {
		Op = op;
	}

	public String getFi() {
		return Fi;
	}

	public void setFi(String fi) {
		Fi = fi;
	}

	public String getFj() {
		return Fj;
	}

	public void setFj(String fj) {
		Fj = fj;
	}

	public String getFk() {
		return Fk;
	}

	public void setFk(String fk) {
		Fk = fk;
	}

	public String getQj() {
		return Qj;
	}

	public void setQj(String qj) {
		Qj = qj;
	}

	public String getQk() {
		return Qk;
	}

	public void setQk(String qk) {
		Qk = qk;
	}

	public boolean isRj() {
		return Rj;
	}

	public void setRj(boolean rj) {
		Rj = rj;
	}

	public boolean isRk() {
		return Rk;
	}

	public void setRk(boolean rk) {
		Rk = rk;
	}

}

class Queue {
	private int[] objs;
	private int head;
	private int end;
	private int size;

	//
	public Queue(int size) {
		objs = new int[size];
		this.head = 0;
		this.end = -1;
		this.size = 0;
	}

	public void push(int instr_count) throws Exception {
		if (this.size > objs.length)
			throw new Exception("Queue is full!");
		if (end == objs.length - 1)
			end = -1;
		objs[++end] = instr_count;
		size++;
	}

	public int pop() throws Exception {
		if (this.size == 0)
			throw new Exception("Queue is empty!");
		int tmp = objs[head++];
		if (head == objs.length)
			head = 0;
		size--;
		return tmp;
	}

	public int peek() throws Exception {
		if (this.size == 0)
			throw new Exception("Queue is empty!");
		return objs[head];
	}

	public int size() {
		return this.size;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	public boolean isFull() {
		return (size == objs.length);
	}
}

public class FunctionUnits {
	public static void main(String[] args) throws Exception {

		FunctionUnit IF = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);
		FunctionUnit ISSUE = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);
		FunctionUnit ALU1 = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);
		FunctionUnit ALU2 = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);
		FunctionUnit MEM = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);
		FunctionUnit WB = new FunctionUnit(null, false, null, null, null, null, null, null, false, false);

		FunctionUnit[] FunctionUnit_State = new FunctionUnit[6];
		FunctionUnit_State[0] = IF;
		FunctionUnit_State[1] = ISSUE;
		FunctionUnit_State[2] = ALU1;
		FunctionUnit_State[3] = ALU2;
		FunctionUnit_State[4] = MEM;
		FunctionUnit_State[5] = WB;

		Queue Pre_issue_queue = new Queue(4);
		Queue Pre_ALU1_queue = new Queue(2);
		Queue Pre_ALU2_queue = new Queue(2);
		Queue Pre_MEM_queue = new Queue(1);
		Queue Post_ALU2_queue = new Queue(1);
		Queue Post_MEM_queue = new Queue(1);

		String input_path = "/home/xinye/桌面/proj/proj2/sample.txt";
		String[] InSet = readtxt(input_path);
		// define register and memory
		int[] GPR = new int[32];
		int[] data = new int[16];
		String[] GPR_state = new String[32];
		// define the initial PC num
		int PC = 256;
		int data_address = 256;

		// decode,and return disassemble Strings
		String[] deassems = decode(InSet, data, PC, data_address);
		// Simulation and will change the register and memeory
		// and will write the number to file every Cycle
		// GPR_and_Data(InSet, GPR, data, PC, "simulation.txt",data_address);

		for (int i = 0; deassems[i] != null; i++) {
			System.out.println(deassems[i]);
		}

		int Cycle = 1;
		int instr_count = 0;
		boolean is_go = true;

		int[] ALU1_cache = null;
		int[] ALU2_cache = null;

		while (is_go) {
			System.out.println("cycle:" + Cycle);

			WriteBack(ALU1_cache, ALU2_cache, GPR, data, FunctionUnit_State);

			ALU1(InSet, Pre_ALU1_queue, Pre_MEM_queue, data, GPR, data_address);

			ALU2(InSet, Pre_ALU2_queue, GPR, data);

			Exe_ISSUE(InSet, FunctionUnit_State, Pre_issue_queue, Pre_ALU1_queue, Pre_ALU2_queue, GPR_state);

			instr_count = Execute_IF(deassems, Pre_issue_queue, false, instr_count, GPR_state, GPR, PC);
			//
			if (Cycle > 30) {
				is_go = false;
			}
			
			/***********************/
			Queue show_temp = new Queue(4) ;
			System.out.println("IF Unit:");
			System.out.println("Pre-Issue Que:");
			while (!Pre_issue_queue.isEmpty()) {
				show_temp.push(Pre_issue_queue.pop());
				System.out.println(deassems[show_temp.peek()]);	
			}
			while( show_temp!= null && !show_temp.isEmpty()) {
				Pre_issue_queue.push(show_temp.pop());
			}
			
			System.out.println("Pre-ALU1 Que");
			while (!Pre_ALU1_queue.isEmpty()) {
				show_temp.push(Pre_ALU1_queue.pop());
				System.out.println(deassems[show_temp.peek()]);	
			}
			while(show_temp!= null && !show_temp.isEmpty()) {
				Pre_ALU1_queue.push(show_temp.pop());
			}
			
			System.out.println("Pre-ALU2 Que:");
			while (!Pre_ALU2_queue.isEmpty()) {
				show_temp.push(Pre_ALU2_queue.pop());
				System.out.println(deassems[show_temp.peek()]);	
			}
			while(show_temp!= null && !show_temp.isEmpty()) {
				Pre_ALU2_queue.push(show_temp.pop());
			}
			
			
			// show reg
			System.out.println("GPR");
			/*
			for (int i = 0; i < GPR.length; i++) {
				if (i % 8 == 0) {
					System.out.println("");
				}
				System.out.print(GPR[i] + "\t");
			}
			// show data
			System.out.println("Data");
			for (int i = 0; i < data.length; i++) {
				if (i % 8 == 0) {
					System.out.println("");
				}
				System.out.print(data[i] + "\t");
			}
*/
			Cycle++;
		}
	}

	private static int Execute_IF(String[] InstructionSet, Queue Pre_issue_queue, boolean is_stall_last,
			int instr_count, String[] reg_state, int[] GPR, int PC) throws Exception {
		// TODO Auto-generated method stub

		String WaitingInstr;
		String ExecutedInstr;
		int Instru_1;
		int Instru_2;

		if (is_stall_last) {
//			return instr_count;
		}

		else {
			Instru_1 = instr_count++;
			if (is_BranchInstru(Instru_1, InstructionSet)) {

				// 解析出来指令码及指令类型
				String in_type = InstructionSet[Instru_1].substring(0, 2);
				String in_opcode = InstructionSet[Instru_1].substring(2, 6);
				// 解析需要的操作数
				int rs_6_11 = Integer.parseInt(InstructionSet[Instru_1].substring(6, 11), 2);
				int base = rs_6_11;
				int rt_11_16 = Integer.parseInt(InstructionSet[Instru_1].substring(11, 16), 2);
				int rd_16_21 = Integer.parseInt(InstructionSet[Instru_1].substring(16, 21), 2);
				// SLL sa
				int sa = Integer.parseInt(InstructionSet[Instru_1].substring(21, 26), 2);

				int immediate = Integer.parseInt(InstructionSet[Instru_1].substring(16, 32), 2);
				int offset = immediate;
				int offset_left2 = offset << 2;

				switch (in_opcode) {
				// J
				case "0000":
					int instr_index = Integer.parseInt(InstructionSet[Instru_1].substring(6, 32), 2) << 2;
					// code = "J " + "#" + instr_index;

					// instr_count = instr_index;
					instr_count = (instr_index - PC) / 4;
					break;
				// JR
				case "0001":
					// code = "JR " + "R" + rs_6_11;
					if (reg_state[rs_6_11] != null) {
						is_stall_last = true;
					} else {
						
						// instr_count = GPR[rs_6_11];
						instr_count = (GPR[rs_6_11] - PC) / 4;
						is_stall_last =false ;
					}
					break;
				// BEQ
				case "0010":
					// code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
					if (reg_state[rs_6_11] != null || reg_state[rt_11_16] != null) {
						is_stall_last = true;
					} else {
						if (GPR[rs_6_11] == GPR[rt_11_16])
							// instr_count = PC + offset_left2 + 4;
							instr_count = (offset_left2 + 4) / 4;
						
						is_stall_last = false ;
					}
					break;

				// BLTZ
				case "0011":
					// code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;
					if (reg_state[rs_6_11] != null) {
						is_stall_last = true;
					} else {
						if (GPR[rs_6_11] < 0)
							// instr_count = PC + offset_left2 + 4;
							instr_count = (offset_left2 + 4) / 4;
						is_stall_last = false;
					}
					break;
				// BGTZ
				case "0100":
					// code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;
					if (reg_state[rs_6_11] != null) {
						is_stall_last = true;
					} else {
						if (GPR[rs_6_11] > 0)
							// instr_count = PC + offset_left2 + 4;
							instr_count = (offset_left2 + 4) / 4;
						is_stall_last = false;
					}
					break;
				}

			}
			// instru1 is not branch
			else {
				Pre_issue_queue.push(Instru_1);
				System.out.println(InstructionSet[Instru_1]);

				Instru_2 = instr_count++;

				if (is_BranchInstru(Instru_2, InstructionSet)) {

					// 解析出来指令码及指令类型
					String in_type = InstructionSet[Instru_2].substring(0, 2);
					String in_opcode = InstructionSet[Instru_2].substring(2, 6);
					// 解析需要的操作数
					int rs_6_11 = Integer.parseInt(InstructionSet[Instru_2].substring(6, 11), 2);
					int base = rs_6_11;
					int rt_11_16 = Integer.parseInt(InstructionSet[Instru_2].substring(11, 16), 2);
					int rd_16_21 = Integer.parseInt(InstructionSet[Instru_2].substring(16, 21), 2);
					// SLL sa
					int sa = Integer.parseInt(InstructionSet[Instru_2].substring(21, 26), 2);

					int immediate = Integer.parseInt(InstructionSet[Instru_2].substring(16, 32), 2);
					int offset = immediate;
					int offset_left2 = offset << 2;
					// J
					int instr_index = Integer.parseInt(InstructionSet[Instru_2].substring(6, 32), 2) << 2;
					switch (in_opcode) {
					// J
					case "0000":

						// code = "J " + "#" + instr_index;

						// instr_count = instr_index;
						instr_count = (instr_index - PC) / 4;
						break;
					// JR
					case "0001":
						// code = "JR " + "R" + rs_6_11;
						if (reg_state[rs_6_11] != null) {
							is_stall_last = true;
						} else {
							// instr_count = GPR[rs_6_11];
							instr_count = (GPR[rs_6_11] - PC) / 4;
						}
						break;
					// BEQ
					case "0010":
						// code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
						if (reg_state[rs_6_11] != null || reg_state[rt_11_16] != null) {
							is_stall_last = true;
						} else {
							if (GPR[rs_6_11] == GPR[rt_11_16])
								// instr_count = PC + offset_left2 + 4;
								instr_count = (offset_left2 + 4) / 4;
						}
						break;

					// BLTZ
					case "0011":
						// code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						if (reg_state[rs_6_11] != null) {
							is_stall_last = true;
						} else {
							if (GPR[rs_6_11] < 0)
								// instr_count = PC + offset_left2 + 4;
								instr_count = (offset_left2 + 4) / 4;
						}
						break;
					// BGTZ
					case "0100":
						// code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						if (reg_state[rs_6_11] != null) {
							is_stall_last = true;
						} else {
							if (GPR[rs_6_11] > 0)
								// instr_count = PC + offset_left2 + 4;
								instr_count = (offset_left2 + 4) / 4;
						}
						break;
					}
				} else {

					Pre_issue_queue.push(Instru_2);
					System.out.println(InstructionSet[Instru_2]);
				}
			}
		}
		return instr_count;
	}

	private static void Exe_ISSUE(String[] InstructionSet, FunctionUnit[] FunctionUnit_State, Queue Pre_issue_queue,
			Queue Pre_ALU1_Queue, Queue Pre_ALU2_Queue, String[] Reg_state) throws Exception {

		boolean issued_ALU1 = false;
		boolean issued_ALU2 = false;

		if (Pre_issue_queue.isEmpty()) {
			return;
		}
		int Instru_1_addr = Pre_issue_queue.pop();
		int Instru_2_addr = 0;
		if (!Pre_issue_queue.isEmpty())
			Instru_2_addr = Pre_issue_queue.pop();

		String Instru_1 = InstructionSet[Instru_1_addr];
		String Instru_2 = InstructionSet[Instru_2_addr];
		// ******** instrunction 1
		// 解析出来指令码及指令类型
		String in_type = Instru_1.substring(0, 2);
		String in_opcode = Instru_1.substring(2, 6);
		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Instru_1.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Instru_1.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Instru_1.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Instru_1.substring(21, 26), 2);

		int immediate = Integer.parseInt(Instru_1.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		// the istruction is SW or LW
		if (in_opcode.equals("0110") || in_opcode.equals("0111")) {
			if (!issued_ALU1 && !FunctionUnit_State[2].isBusy()) {
				Pre_ALU1_Queue.push(Instru_1_addr);
				issued_ALU1 = true;
			} else {
				Pre_issue_queue.push(Instru_1_addr);

				// set functionState
				FunctionUnit_State[2].setBusy(true);
				FunctionUnit_State[2].setOp(in_opcode);
				if (in_opcode.equals("0111")) {
					Reg_state[rt_11_16] = "0111";
				}
			}
		} else {
			if (!issued_ALU2 && !FunctionUnit_State[3].isBusy()) {
				Pre_ALU2_Queue.push(Instru_1_addr);
				issued_ALU2 = true;
			} else {
				Pre_issue_queue.push(Instru_1_addr);
				FunctionUnit_State[3].setBusy(true);
				FunctionUnit_State[3].setOp(in_opcode);

				switch (in_opcode) {
				// ADD
				case "0000":
					// code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
					Reg_state[rd_16_21] = "0000";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);
					break;
				// SUB
				case "0001":
					// code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
					Reg_state[rd_16_21] = "0001";

					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// MUL
				case "0010":
					// code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

					Reg_state[rd_16_21] = "0010";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// AND
				case "0011":
					// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

					Reg_state[rd_16_21] = "0011";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// OR
				case "0100":
					// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

					Reg_state[rd_16_21] = "0100";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// XOR
				case "0101":
					// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

					Reg_state[rd_16_21] = "0101";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// NOR
				case "0110":
					// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

					// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
					break;
				// SLT
				case "0111":
					// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
					//
					// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
					break;
				// ADDI
				case "1000":
					// code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

					Reg_state[rd_16_21] = "0000";
					FunctionUnit_State[3].setFj("F" + rs_6_11);
					// FunctionUnit_State[3].setFk("F" + rt_11_16);

					FunctionUnit_State[3].setRj(true);
					FunctionUnit_State[3].setRk(true);

					break;
				// ANDI
				case "1001":
					// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

					// GPR[rd_16_21] = GPR[rs_6_11] & immediate;
					break;
				// ORI
				case "1010":
					// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

					// GPR[rt_11_16] = GPR[rs_6_11] | immediate;
					break;
				// XORI
				case "1011":
					// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

					// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
					break;
				// default
				default:
					break;
				}
			}
		}

		// 解析出来指令码及指令类型
		in_type = Instru_2.substring(0, 2);
		in_opcode = Instru_2.substring(2, 6);

		if (in_opcode.equals("0110") || in_opcode.equals("0111")) {
			if (!issued_ALU1 && !FunctionUnit_State[2].isBusy()) {
				Pre_ALU1_Queue.push(Instru_2_addr);
				issued_ALU1 = true;
			} else {
				Pre_ALU1_Queue.push(Instru_2_addr);
			}
		} else {
			if (!issued_ALU2 && !FunctionUnit_State[3].isBusy()) {
				Pre_ALU2_Queue.push(Instru_2_addr);
				issued_ALU2 = true;
			} else {
				Pre_ALU2_Queue.push(Instru_2_addr);
			}
		}
	}

	private static boolean is_BranchInstru(int instru, String[] Inset) {
		// TODO Auto-generated method stub
		// 解析出来指令码及指令类型
		String in_type = Inset[instru].substring(0, 2);
		String in_opcode = Inset[instru].substring(2, 6);

		if (in_type.equals("01") && (in_opcode.equals("0000") || in_opcode.equals("0001") || in_opcode.equals("0010")
				|| in_opcode.equals("0010") || in_opcode.equals("0011") || in_opcode.equals("0100")))
			return true;
		else
			return false;
	}

	public static String[] decode(String[] Inst, int[] data, int PC, int data_address) {
		int len = Inst.length;
		String[] string_result = new String[len];
		int address = 256;
		// int data_address = 0 ;
		int data_count = 0;
		boolean is_break = false;
		for (int i = 0; Inst[i] != null; i++) {
			if (!is_break) {
				String in_type = Inst[i].substring(0, 2);
				String in_opcode = Inst[i].substring(2, 6);

				int rs_6_11 = Integer.parseInt(Inst[i].substring(6, 11), 2);
				int base = rs_6_11;
				int rt_11_16 = Integer.parseInt(Inst[i].substring(11, 16), 2);
				int rd_16_21 = Integer.parseInt(Inst[i].substring(16, 21), 2);
				// SLL sa
				int sa = Integer.parseInt(Inst[i].substring(21, 26), 2);

				int immediate = Integer.parseInt(Inst[i].substring(16, 32), 2);
				int offset = immediate;
				int offset_left2 = offset << 2;
				// J target
				int instr_index = Integer.parseInt(Inst[i].substring(6, 32), 2) << 2;

				if (in_type.equals("01")) {
					String inst_and_add = Inst[i] + "\t" + address + "\t";
					String code;
					switch (in_opcode) {
					// J
					case "0000":
						code = "J " + "#" + instr_index;
						string_result[i] = inst_and_add + code;
						break;
					// JR
					case "0001":
						code = "JR " + "R" + rs_6_11;
						string_result[i] = inst_and_add + code;
						break;
					// BEQ
					case "0010":
						code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
						string_result[i] = inst_and_add + code;
						break;
					// BLTZ
					case "0011":
						code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						string_result[i] = inst_and_add + code;
						break;
					// BGTZ
					case "0100":
						code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						string_result[i] = inst_and_add + code;
						break;
					// BREAK
					case "0101":
						code = "BREAK";
						is_break = true;
						string_result[i] = inst_and_add + code;
						data_address = address + 4;
						break;
					// SW
					case "0110":
						code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
						string_result[i] = inst_and_add + code;
						break;
					// LW
					case "0111":
						code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
						string_result[i] = inst_and_add + code;
						break;
					// SLL
					case "1000":
						code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						string_result[i] = inst_and_add + code;
						break;
					// SRL
					case "1001":
						code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						string_result[i] = inst_and_add + code;
						break;
					// SRA
					case "1010":
						code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						string_result[i] = inst_and_add + code;
						break;
					// NOP
					case "1011":
						code = "NOP";
						string_result[i] = inst_and_add + code;
						break;
					// default
					default:
						break;
					}

				} else if (in_type.equals("11")) {
					String inst_and_add = Inst[i] + "\t" + address + "\t";
					String code;
					switch (in_opcode) {
					// ADD
					case "0000":
						code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// SUB
					case "0001":
						code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// MUL
					case "0010":
						code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// AND
					case "0011":
						code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// OR
					case "0100":
						code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// XOR
					case "0101":
						code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// NOR
					case "0110":
						code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// SLT
					case "0111":
						code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						string_result[i] = inst_and_add + code;
						break;
					// ADDI
					case "1000":
						code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						string_result[i] = inst_and_add + code;
						break;
					// ANDI
					case "1001":
						code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						string_result[i] = inst_and_add + code;
						break;
					// ORI
					case "1010":
						code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						string_result[i] = inst_and_add + code;
						break;
					// XORI
					case "1011":
						code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						string_result[i] = inst_and_add + code;
						break;
					// default
					default:
						break;
					}
				}

				address += 4;
			}
			// reach to the data code
			else {
				String inst_and_add = Inst[i] + "\t" + address + "\t";
				int data_temp;
				int symbol = Integer.parseInt(Inst[i].substring(0, 1));
				if (symbol == 0) {
					data_temp = (Integer.parseInt(Inst[i].substring(0, 32), 2));
				} else {
					data_temp = (Integer.parseInt(Inst[i].substring(1, 32), 2) - Integer.MAX_VALUE - 1);
				}

				string_result[i] = inst_and_add + data_temp;
				data[data_count] = data_temp;

				data_count++;
				address += 4;
			}
		}
		return string_result;
	}

	private static void GPR_and_Data(String[] Inst, int[] GPR, int[] data, int PC, String OutPutFile,
			int data_address) {
		// TODO Auto-generated method stub
		int PC_now = PC;
		int len = Inst.length;
		String[] string_result = new String[len];
		int PC_base = PC;
		boolean is_break = false;
		int cycle = 1;
		for (int i = 0; !is_break; i = ((PC_now - PC_base) / 4)) {
			// System.out.println("--------------------");
			// System.out.print("Cycle:" + cycle + "\t" + PC_now + "\t");
			String temp;
			String code = null;
			if (!is_break) {
				// 解析出来指令码及指令类型
				String in_type = Inst[i].substring(0, 2);
				String in_opcode = Inst[i].substring(2, 6);
				// 解析需要的操作数
				int rs_6_11 = Integer.parseInt(Inst[i].substring(6, 11), 2);
				int base = rs_6_11;
				int rt_11_16 = Integer.parseInt(Inst[i].substring(11, 16), 2);
				int rd_16_21 = Integer.parseInt(Inst[i].substring(16, 21), 2);
				// SLL sa
				int sa = Integer.parseInt(Inst[i].substring(21, 26), 2);

				int immediate = Integer.parseInt(Inst[i].substring(16, 32), 2);
				int offset = immediate;
				int offset_left2 = offset << 2;

				// J target
				int instr_index = Integer.parseInt(Inst[i].substring(6, 32), 2) << 2;

				if (in_type.equals("01")) {
					switch (in_opcode) {
					// J
					case "0000":
						code = "J " + "#" + instr_index;

						PC_now = instr_index;
						break;
					// JR
					case "0001":
						code = "JR " + "R" + rs_6_11;

						PC_now = GPR[rs_6_11];
						break;
					// BEQ
					case "0010":
						code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;

						if (GPR[rs_6_11] == GPR[rt_11_16])
							PC_now = PC + offset_left2 + 4;
						break;
					// BLTZ
					case "0011":
						code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;

						if (GPR[rs_6_11] < 0)
							PC_now = PC + offset_left2 + 4;
						break;
					// BGTZ
					case "0100":
						code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;

						if (GPR[rs_6_11] > 0)
							PC_now = PC + offset_left2 + 4;
						break;
					// BREAK
					case "0101":
						code = "BREAK";
						// data_address = inst_and_add + 4 ;
						is_break = true;
						break;
					// SW
					case "0110":
						code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

						data[(offset - data_address + GPR[base]) / 4] = GPR[rt_11_16];
						break;
					// LW
					case "0111":
						code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

						GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];
						break;
					// SLL
					case "1000":
						code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

						GPR[rd_16_21] = GPR[rt_11_16] << sa;
						break;
					// SRL
					case "1001":
						code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

						GPR[rd_16_21] = GPR[rt_11_16] >> sa;
						break;
					// SRA
					case "1010":
						code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

						// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
						int xsrl = GPR[rt_11_16] >> sa;
						int w = 4 << 3;
						GPR[rd_16_21] |= (-1 << (w - sa));
						break;
					// NOP
					case "1011":
						code = "NOP";

						break;
					// default
					default:

						break;
					}

				} else if (in_type.equals("11")) {
					switch (in_opcode) {
					// ADD
					case "0000":
						code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						try {
							GPR[rd_16_21] = GPR[rs_6_11] + GPR[rt_11_16];
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("SignalException(IntegerOverflow)");
						}
						break;
					// SUB
					case "0001":
						code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						try {
							GPR[rd_16_21] = GPR[rs_6_11] - GPR[rt_11_16];
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("SignalException(IntegerOverflow)");
						}
						break;
					// MUL
					case "0010":
						code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						try {
							GPR[rd_16_21] = GPR[rs_6_11] * GPR[rt_11_16];
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("SignalException(IntegerOverflow)");
						}
						break;
					// AND
					case "0011":
						code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
						break;
					// OR
					case "0100":
						code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
						break;
					// XOR
					case "0101":
						code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
						break;
					// NOR
					case "0110":
						code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
						break;
					// SLT
					case "0111":
						code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

						GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
						break;
					// ADDI
					case "1000":
						code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

						try {
							GPR[rt_11_16] = GPR[rs_6_11] + immediate;
						} catch (Exception e) {
							// TODO: handle exception
							System.out.println("SignalException(IntegerOverflow)");
						}
						break;
					// ANDI
					case "1001":
						code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

						GPR[rd_16_21] = GPR[rs_6_11] & immediate;
						break;
					// ORI
					case "1010":
						code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

						GPR[rt_11_16] = GPR[rs_6_11] | immediate;
						break;
					// XORI
					case "1011":
						code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

						GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
						break;
					// default
					default:

						break;
					}
				}
				// PC is the instrument`s pc,so it is PC ,not PC_now
				temp = "--------------------" + '\n' + "Cycle:" + cycle + "\t" + PC + "\t" + code;
				// WriteSimulation(OutPutFile, GPR, temp + '\n' + '\n' + "Registers", "r",
				// data_address);
				// WriteSimulation(OutPutFile, data, "Data", "d",data_address);

			} else {

				break;
			}

			PC_now = (PC_now == PC) ? (PC_now + 4) : PC_now;
			PC = PC_now;
			cycle++;
		}

	}

	private static int[] ALU1(String[] InstruSet, Queue Pre_ALU1_queue, Queue pre_Mem, int[] data, int[] GPR,
			int data_address) throws Exception {

		if (Pre_ALU1_queue.isEmpty()) {
			return null;
		}

		String code = null;

		int[] cache = new int[2];
		int Inst_addr = Pre_ALU1_queue.pop();
		String Inst = InstruSet[Inst_addr];
		// 解析出来指令码及指令类型
		// 解析出来指令码及指令类型
		String in_type = Inst.substring(0, 2);
		String in_opcode = Inst.substring(2, 6);
		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Inst.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Inst.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Inst.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Inst.substring(21, 26), 2);

		int immediate = Integer.parseInt(Inst.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		// J target
		int instr_index = Integer.parseInt(Inst.substring(6, 32), 2) << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// data[(offset - data_address + GPR[base]) / 4] = GPR[rt_11_16];
				cache[0] = (offset - data_address + GPR[base]) / 4;
				cache[1] = GPR[rt_11_16];
				break;
			// LW
			case "0111":
				code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];
				cache[0] = GPR[rt_11_16];
				cache[1] = (offset - data_address + GPR[base]) / 4;
				break;
			// default
			default:

				break;
			}
		}
		return cache;
	}

	private static int[] ALU2(String[] InstruSet, Queue Pre_ALU2_queue, int[] GPR, int[] data) throws Exception {
		if (Pre_ALU2_queue.isEmpty()) {
			return null;
		}
		int Inst_addr = Pre_ALU2_queue.pop();
		String Inst = InstruSet[Inst_addr];
		int[] cache = new int[2];
		// 解析出来指令码及指令类型
		String in_type = Inst.substring(0, 2);
		String in_opcode = Inst.substring(2, 6);
		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Inst.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Inst.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Inst.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Inst.substring(21, 26), 2);

		int immediate = Integer.parseInt(Inst.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		// test

		String code;
		int PC_now;
		// J target
		int instr_index = Integer.parseInt(Inst.substring(6, 32), 2) << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SLL
			case "1000":
				code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				GPR[rd_16_21] = GPR[rt_11_16] << sa;
				cache[0] = rd_16_21;
				cache[1] = GPR[rt_11_16] << sa;
				return cache;
			// SRL
			case "1001":
				code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				cache[0] = rd_16_21;
				cache[1] = GPR[rt_11_16] << sa;
				return cache;
			// SRA
			case "1010":
				code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				int xsrl = GPR[rt_11_16] >> sa;
				int w = 4 << 3;
				GPR[rd_16_21] |= (-1 << (w - sa));
				cache[0] = rd_16_21;
				cache[1] |= (-1 << (w - sa));
				return cache;
			// NOP
			case "1011":
				code = "NOP";

				break;
			// default
			default:

				break;
			}

		} else if (in_type.equals("11")) {
			switch (in_opcode) {
			// ADD
			case "0000":
				code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				try {
					GPR[rd_16_21] = GPR[rs_6_11] + GPR[rt_11_16];
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("SignalException(IntegerOverflow)");
				}
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] + GPR[rt_11_16];
				return cache;
			// SUB
			case "0001":
				code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				try {
					GPR[rd_16_21] = GPR[rs_6_11] - GPR[rt_11_16];
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("SignalException(IntegerOverflow)");
				}
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] - GPR[rt_11_16];
				return cache;
			// MUL
			case "0010":
				code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				try {
					GPR[rd_16_21] = GPR[rs_6_11] * GPR[rt_11_16];
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("SignalException(IntegerOverflow)");
				}
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] * GPR[rt_11_16];
				return cache;
			// AND
			case "0011":
				code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] & GPR[rt_11_16];
				return cache;
			// OR
			case "0100":
				code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] | GPR[rd_16_21];
				return cache;
			// XOR
			case "0101":
				code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] ^ GPR[rd_16_21];
				return cache;
			// NOR
			case "0110":
				code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				cache[0] = rd_16_21;
				cache[1] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				return cache;
			// SLT
			case "0111":
				code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				cache[0] = rd_16_21;
				cache[1] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				return cache;
			// ADDI
			case "1000":
				code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

				try {
					GPR[rt_11_16] = GPR[rs_6_11] + immediate;
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("SignalException(IntegerOverflow)");
				}
				cache[0] = rt_11_16;
				cache[1] = GPR[rs_6_11] + immediate;
				return cache;
			// ANDI
			case "1001":
				code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

				GPR[rd_16_21] = GPR[rs_6_11] & immediate;
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] & immediate;
				return cache;
			// ORI
			case "1010":
				code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

				GPR[rt_11_16] = GPR[rs_6_11] | immediate;
				cache[0] = rt_11_16;
				cache[1] = GPR[rs_6_11] | immediate;
				return cache;
			// XORI
			case "1011":
				code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

				GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;

				cache[0] = rt_11_16;
				cache[1] = GPR[rs_6_11] ^ immediate;
				return cache;
			// default
			default:
				break;
			}
		}
		return null;
	}

	private static void WriteBack(int[] ALU1_cache, int[] ALU2_cache, int[] reg, int[] Data, FunctionUnit[] FucS) {
		if (ALU1_cache == null) {
			System.out.println("ALU1 write null");
		}else {
			int Data_des = ALU1_cache[0];
			int Data_data = ALU1_cache[1];
			
			Data[Data_des] = Data_data;
		}
		if (ALU2_cache == null) {
			System.out.println("ALU2 write null");
		}else {
			int PRG_des = ALU2_cache[0];
			int PRG_data = ALU2_cache[1];
		
			// test
			reg[PRG_des] = PRG_data;
		}
		/*
		 * // WAR for (int i = 0; i < FucS.length; i++) { if
		 * ((FucS[i].getFj().equals(FucS[3].getFi()) || !FucS[i].Rj) &&
		 * (FucS[i].getFk().equals(FucS[3].getFi()) || !FucS[i].Rk)) { reg[PRG_des] =
		 * PRG_data; } }
		 */
	}

	public static String[] readtxt(String input_path) {
		StringBuilder result = new StringBuilder();
		String[] instruments = new String[100];
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(input_path));// 构造一个BufferedReader类来读取文件
			String s = null;
			while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
				result.append(System.lineSeparator() + s);
				instruments[count] = s;
				count++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instruments;
	}

	
}
