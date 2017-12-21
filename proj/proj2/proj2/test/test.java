import java.io.BufferedReader;
import java.io.FileReader;

class QUEUE {
	private int[] objs;
	private int head;
	private int end;
	private int size;
	//
	public QUEUE(int size) {
		objs = new int[size];
		this.head = 0;
		this.end = -1;
		this.size = 0;
	}
	public void push(int instr_count) throws Exception {
		if (this.size > objs.length)
			throw new Exception("QUEUE is full!");
		if (end == objs.length - 1)
			end = -1;
		objs[++end] = instr_count;
		size++;

	}
	public int pop() throws Exception {
		if (this.size == 0)
			throw new Exception("QUEUE is empty!");
		int tmp = objs[head++];
		if (head == objs.length)
			head = 0;
		size--;
		return tmp;
	}
	public int peek() throws Exception {
		if (this.size == 0)
			throw new Exception("QUEUE is empty!");
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

public class test {
	public static void main(String[] args) throws Exception {

		QUEUE Pre_ISSUE_QUEUE = new QUEUE(4);
		QUEUE Waiting_QUEUE = new QUEUE(1);
		QUEUE Executed_QUEUE = new QUEUE(1);

		QUEUE Pre_ALU1_QUEUE = new QUEUE(2);
		QUEUE Pre_ALU2_QUEUE = new QUEUE(2);
		QUEUE Pre_MEM_QUEUE = new QUEUE(1);
		QUEUE Post_ALU2_QUEUE = new QUEUE(1);
		QUEUE Post_MEM_QUEUE = new QUEUE(1);

		QUEUE ISSUED_INSTRU = new QUEUE(20);

		String input_path = "/home/xinye/桌面/proj/proj2/sample.txt";
		String[] InstrSet = readtxt(input_path);
		// define register and memory
		int[] GPR = new int[32];
		int[] Data = new int[16];
		String[] GPR_state = new String[32];
		int[] element_del = new int[2];
		
		boolean[] update = new boolean[2];
		// define the initial PC num
		int PC = 256;
		int Data_address = 300;

		int[] ALU1_instru = new int[2];
		int[] ALU2_instru = new int[2];

		// decode,and return disassemble Strings
		String[] deassems = decode(InstrSet, Data, PC, Data_address);
		// Simulation and will change the register and memeory
		// and will write the number to file every Cycle
		// GPR_and_Data(InstrSet, GPR, Data, PC, "simulation.txt",Data_address);

		for (int i = 0; deassems[i] != null; i++) {
			System.out.println(deassems[i]);
		}

		int Cycle = 1;
		int instr_count = 0;
		boolean[] is_go = new boolean[2];
		is_go[0] = true;
		boolean[] is_stall_last = new boolean[2];

		int[] ALU1_cache = null;
		int[] ALU2_cache = null;
		int[] MEM_cache = null;
		int issue_sum = 0;
		while (is_go[0]) {
			System.out.println("");
			System.out.println("***********************  cycle : " + Cycle);

			WriteBack(MEM_cache, ALU2_cache, Post_MEM_QUEUE, Post_ALU2_QUEUE, GPR, Data, GPR_state, ISSUED_INSTRU,
					element_del,update);

			MEM_cache = MEM(Pre_MEM_QUEUE, Post_MEM_QUEUE, InstrSet, Data, GPR, Data_address, element_del,update);

			ALU1(InstrSet, Pre_ALU1_QUEUE, Pre_MEM_QUEUE, Data, GPR, Data_address);

			ALU2_cache = ALU2(InstrSet, Pre_ALU2_QUEUE, Post_ALU2_QUEUE, GPR, Data);

			issue_sum = Exe_ISSUE(InstrSet, Pre_ISSUE_QUEUE, Pre_ALU1_QUEUE, Pre_ALU2_QUEUE, GPR_state, ALU1_instru, ALU2_instru,
					ISSUED_INSTRU);

			instr_count = Execute_IF(deassems, Pre_ISSUE_QUEUE, Waiting_QUEUE, Executed_QUEUE, is_stall_last,
					instr_count, GPR_state, GPR, PC, is_go, ISSUED_INSTRU ,issue_sum);
			if (update[0]) {
				Delete_element(ISSUED_INSTRU, element_del);
			}
			//
			if (Cycle > 35) {
				is_go[0] = false;
			}

			ShowQueues(deassems, Waiting_QUEUE, Executed_QUEUE, Pre_ISSUE_QUEUE, Pre_ALU1_QUEUE, Pre_ALU2_QUEUE,
					Pre_MEM_QUEUE, Post_MEM_QUEUE, Post_ALU2_QUEUE);
			ShowResult(GPR_state, GPR, Data);

			Cycle++;
		}
	}

	private static int Execute_IF(String[] InstrSet, QUEUE Pre_ISSUE_QUEUE, QUEUE Waiting_QUEUE, QUEUE Executed_QUEUE,
			boolean[] is_stall_last, int instr_count, String[] reg_state, int[] GPR, int PC, boolean[] is_go,
			QUEUE ISSUED_INSTRU, int issue_sum) throws Exception {
		// TODO Auto-generated method stub
		is_stall_last[0] = false;
		String WaitingInstr;
		String ExecutedInstr;
		int FetchInstr1;
		int FetchInstr2;
		int[] goto_instru = new int[2];

		if (!Executed_QUEUE.isEmpty()) {
			Executed_QUEUE.pop();
		}
		
		if (!Waiting_QUEUE.isEmpty()) {
			int Waiting_Instru = Waiting_QUEUE.pop();
			if (is_BranchInstru(Waiting_Instru, InstrSet, goto_instru, PC, reg_state, GPR, is_stall_last)) {

				if (!is_stall_last[0]) {
					instr_count = goto_instru[0];
					Executed_QUEUE.push(Waiting_Instru);
					
					return instr_count;
				} else {
					Waiting_QUEUE.push(Waiting_Instru);
					return Waiting_Instru;
				}
			}

		} else {
			switch (4 - Pre_ISSUE_QUEUE.size() - issue_sum) {
			case 0:
				return instr_count;
			case 1:
//				FetchInstr1 = Executed_QUEUE.isEmpty() ? instr_count++ : Executed_QUEUE.pop();
				FetchInstr1 = instr_count ++ ;
				// is BREAK
				if (is_BREAK(FetchInstr1, InstrSet)) {
					is_go[0] = false;
					Executed_QUEUE.push(FetchInstr1);
					break;
				}
				// is not BREAK
				if (is_BranchInstru(FetchInstr1, InstrSet, goto_instru, PC, reg_state, GPR, is_stall_last)) {
					if (!is_stall_last[0]) {
						instr_count = goto_instru[0];
						// Pre_ISSUE_QUEUE.push(instr_count++);
						Executed_QUEUE.push(FetchInstr1);
						return instr_count;
						// break ;
					} else {
						Waiting_QUEUE.push(FetchInstr1);
						return FetchInstr1;
					}

				} else {
					Pre_ISSUE_QUEUE.push(FetchInstr1);
					Harzed(FetchInstr1, InstrSet, reg_state);
				}
				break;
			case 2:
			case 3:
			case 4:
				// get the first instru
//				FetchInstr1 = Executed_QUEUE.isEmpty() ? instr_count++ : Executed_QUEUE.pop();
				FetchInstr1 = instr_count ++ ;
				// is BREAK
				if (is_BREAK(FetchInstr1, InstrSet)) {
					is_go[0] = false;
					Executed_QUEUE.push(FetchInstr1);
					break;
				}

				if (is_BranchInstru(FetchInstr1, InstrSet, goto_instru, PC, reg_state, GPR, is_stall_last)) {
					if (!is_stall_last[0]) {
						instr_count = goto_instru[0];
						// Pre_ISSUE_QUEUE.push(instr_count++);
						Executed_QUEUE.push(FetchInstr1);
						return instr_count;
						// break;
					} else {
						Waiting_QUEUE.push(FetchInstr1);
						return FetchInstr1;
					}
				} else {
					Pre_ISSUE_QUEUE.push(FetchInstr1);
					Harzed(FetchInstr1, InstrSet, reg_state);
				}
				// get the second instr
				FetchInstr2 = instr_count++;
				// is BREAK
				if (is_BREAK(FetchInstr2, InstrSet)) {
					is_go[0] = false;
					Executed_QUEUE.push(FetchInstr2);
					break;
				}

				if (is_BranchInstru(FetchInstr2, InstrSet, goto_instru, PC, reg_state, GPR, is_stall_last)) {
					if (!is_stall_last[0]) {
						instr_count = goto_instru[0];
						Executed_QUEUE.push(FetchInstr2);
						return instr_count;
					} else {
						Waiting_QUEUE.push(FetchInstr2);
						return FetchInstr2;
					}
				} else {
					Pre_ISSUE_QUEUE.push(FetchInstr2);
					Harzed(FetchInstr2, InstrSet, reg_state);
				}
				break;
			default:
				break;
			}

		}
	
		return instr_count;

	}

	private static int Exe_ISSUE(String[] InstruSet, QUEUE Pre_ISSUE_QUEUE, QUEUE Pre_ALU1_QUEUE, QUEUE Pre_ALU2_QUEUE,
			String[] GPR_state, int[] ALU1_instru, int[] ALU2_instru, QUEUE ISSUED_INSTRU) throws Exception {

		boolean issued_ALU1 = false;
		boolean issued_ALU2 = false;
		int Instru_1_addr = 0;
		int Instru_2_addr = 0;

		QUEUE Issue_temp = new QUEUE(4);
		QUEUE Issue_temp_this = new QUEUE(4);

		QUEUE temp = new QUEUE(20);

		int issue_sum = 0;
		int issued_instru_addr = 0;

		if (Pre_ALU1_QUEUE.isFull() && Pre_ALU2_QUEUE.isFull()) {
			return issue_sum;
		}

		while (!Pre_ISSUE_QUEUE.isEmpty()) {
			boolean flag = false;
			Instru_1_addr = Pre_ISSUE_QUEUE.pop();
			// WAW with not issued
			while (!Issue_temp.isEmpty()) {
				int instr_temp = Issue_temp.peek();
				temp.push(Issue_temp.pop());
				if (Harzed_WAW(Instru_1_addr, InstruSet, instr_temp) || Harzed_WAR(Instru_1_addr, InstruSet, instr_temp)
						|| Harzed_WAR(instr_temp, InstruSet, Instru_1_addr)) {
					flag = true;
					break;
				}
			}
			while (!Issue_temp.isEmpty()) {
				temp.push(Issue_temp.pop());
			}
			while (!temp.isEmpty()) {
				Issue_temp.push(temp.pop());
			}

			// WAW with issued
			while (!flag && !ISSUED_INSTRU.isEmpty()) {
				int instr_temp = ISSUED_INSTRU.pop();
				temp.push(instr_temp);
				if (Harzed_WAW(Instru_1_addr, InstruSet, instr_temp) || Harzed_WAR(Instru_1_addr, InstruSet, instr_temp)
						|| Harzed_WAR(instr_temp, InstruSet, Instru_1_addr)) {
					flag = true;
					break;
				}

			}
			while (!ISSUED_INSTRU.isEmpty()) {
				temp.push(ISSUED_INSTRU.pop());
			}
			while (!temp.isEmpty()) {
				ISSUED_INSTRU.push(temp.pop());
			}

			if (!flag) {
				if (is_SW_LW(Instru_1_addr, InstruSet) && !issued_ALU1 && !Pre_ALU1_QUEUE.isFull()) {
					Pre_ALU1_QUEUE.push(Instru_1_addr);
					ISSUED_INSTRU.push(Instru_1_addr);
					// Harzed(Instru_1_addr, InstruSet, GPR_state);
					issued_ALU1 = true;
					issue_sum ++ ;
				} else if (!is_SW_LW(Instru_1_addr, InstruSet) && !issued_ALU2 && !Pre_ALU2_QUEUE.isFull()) {
					Pre_ALU2_QUEUE.push(Instru_1_addr);
					ISSUED_INSTRU.push(Instru_1_addr);
					// Harzed(Instru_1_addr, InstruSet, GPR_state);
					issued_ALU2 = true;
					issue_sum ++ ;
				} else {
					Issue_temp.push(Instru_1_addr);
				}
			} else {
				Issue_temp.push(Instru_1_addr);
			}
		}
		// to keep the order
		while (!Pre_ISSUE_QUEUE.isEmpty()) {
			Issue_temp.push(Pre_ISSUE_QUEUE.pop());
		}
		while (!Issue_temp.isEmpty()) {
			Pre_ISSUE_QUEUE.push(Issue_temp.pop());
		}
		return issue_sum;
	}

	private static int[] ALU1(String[] InstruSet, QUEUE Pre_ALU1_QUEUE, QUEUE Pre_MEM_QUEUE, int[] Data, int[] GPR,
			int Data_address) throws Exception {

		if (Pre_ALU1_QUEUE.isEmpty()) {
			return null;
		}

		String code = null;

		int[] cache = new int[2];
		int Inst_addr = Pre_ALU1_QUEUE.pop();
		Pre_MEM_QUEUE.push(Inst_addr);

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

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// Data[(offset - Data_address + GPR[base]) / 4] = GPR[rt_11_16];
				cache[0] = (offset - Data_address + GPR[base]) / 4;
				cache[1] = GPR[rt_11_16];
				break;
			// LW
			case "0111":
				code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// GPR[rt_11_16] = Data[(offset - Data_address + GPR[base]) / 4];

				break;
			// default
			default:

				break;
			}
		}
		return cache;
	}

	private static int[] ALU2(String[] InstruSet, QUEUE Pre_ALU2_QUEUE, QUEUE Post_ALU2_QUEUE, int[] GPR, int[] Data)
			throws Exception {
		if (Pre_ALU2_QUEUE.isEmpty() || Post_ALU2_QUEUE.isFull()) {
			return null;
		}
		int Inst_addr = Pre_ALU2_QUEUE.pop();
		Post_ALU2_QUEUE.push(Inst_addr);

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
				// code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				//
				// GPR[rd_16_21] = GPR[rt_11_16] << sa;
				cache[0] = rd_16_21;
				cache[1] = GPR[rt_11_16] << sa;
				return cache;
			// SRL
			case "1001":
				// code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				//
				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				cache[0] = rd_16_21;
				cache[1] = GPR[rt_11_16] << sa;
				return cache;
			// SRA
			case "1010":
				// code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				//
				// // GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				int xsrl = GPR[rt_11_16] >> sa;
				int w = 4 << 3;
				// GPR[rd_16_21] |= (-1 << (w - sa));
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
				// code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] + GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] + GPR[rt_11_16];
				return cache;
			// SUB
			case "0001":
				// code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] - GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] - GPR[rt_11_16];
				return cache;
			// MUL
			case "0010":
				// code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] * GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] * GPR[rt_11_16];
				return cache;
			// AND
			case "0011":
				// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] & GPR[rt_11_16];
				return cache;
			// OR
			case "0100":
				// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] | GPR[rd_16_21];
				return cache;
			// XOR
			case "0101":
				// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] ^ GPR[rd_16_21];
				return cache;
			// NOR
			case "0110":
				// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				cache[0] = rd_16_21;
				cache[1] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				return cache;
			// SLT
			case "0111":
				// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				//
				// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				cache[0] = rd_16_21;
				cache[1] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				return cache;
			// ADDI
			case "1000":
				// code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				//
				// try {
				// GPR[rt_11_16] = GPR[rs_6_11] + immediate;
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				cache[0] = rt_11_16;
				cache[1] = GPR[rs_6_11] + immediate;
				return cache;
			// ANDI
			case "1001":
				// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				//
				// GPR[rd_16_21] = GPR[rs_6_11] & immediate;
				cache[0] = rd_16_21;
				cache[1] = GPR[rs_6_11] & immediate;
				return cache;
			// ORI
			case "1010":
				// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				//
				// GPR[rt_11_16] = GPR[rs_6_11] | immediate;
				cache[0] = rt_11_16;
				cache[1] = GPR[rs_6_11] | immediate;
				return cache;
			// XORI
			case "1011":
				// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				//
				// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;

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

	private static int[] MEM(QUEUE pre_MEM_QUEUE, QUEUE post_MEM_QUEUE, String[] InstruSet, int[] Data, int[] GPR,
			int Data_address, int[] element_del,boolean[] update) throws Exception {
		// TODO Auto-generated method stub
		if (pre_MEM_QUEUE.isEmpty()) {
			return null;
		} else {
			int Inst_addr = pre_MEM_QUEUE.pop();

			String Inst = InstruSet[Inst_addr];
			int[] cache = new int[2];
			// decode
			String in_type = Inst.substring(0, 2);
			String in_opcode = Inst.substring(2, 6);

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

			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				Data[(offset - Data_address + GPR[base]) / 4] = GPR[rt_11_16];
				// cache[0] = (offset - Data_address + GPR[base]) / 4 ;
				// cache[1] = rt_11_16 ;
				element_del[0] = Inst_addr;
				update[0] = true;
				return null;
			// LW
			case "0111":
				// code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				// GPR[rt_11_16] = Data[(offset - Data_address + GPR[base]) / 4];
				cache[0] = rt_11_16;
				cache[1] = Data[(offset - Data_address + GPR[base]) / 4];
				post_MEM_QUEUE.push(Inst_addr);
				return cache;
			default:
				break;
			}
		}
		return null;

	}

	private static void WriteBack(int[] MEM_cache, int[] ALU2_cache, QUEUE Post_MEM_QUEUE, QUEUE Post_ALU2_QUEUE,
			int[] GPR, int[] Data, String[] GPR_state, QUEUE ISSUED_INSTRU, int[] element_del,boolean[] update) throws Exception {
		if (MEM_cache == null) {
			System.out.println("ALU1 write null");
		} else {

			int Data_des = MEM_cache[0];
			int Data_Data = MEM_cache[1];
			GPR[Data_des] = Data_Data;
			GPR_state[Data_des] = null;
			// Delete_element(ISSUED_INSTRU, Post_MEM_QUEUE.pop() );
			element_del[0] = Post_MEM_QUEUE.pop();
			update[0] = true;
		}

		if (ALU2_cache == null) {
			System.out.println("ALU2 write null");
		} else {
			// Post_ALU2_QUEUE.pop();
			int PRG_des = ALU2_cache[0];
			int PRG_Data = ALU2_cache[1];
			// test
			GPR[PRG_des] = PRG_Data;
			GPR_state[PRG_des] = null;
			element_del[1] = Post_ALU2_QUEUE.pop();
			update[0] = true;
		}
	}

	private static void Delete_element(QUEUE ISSUED_INSTRU, int[] element_del) throws Exception {
		// TODO Auto-generated method stub
		QUEUE temp = new QUEUE(20);
		
		for (int element : element_del) {
			
		while (!ISSUED_INSTRU.isEmpty()) {
			if (element == ISSUED_INSTRU.peek()) {
				ISSUED_INSTRU.pop();
			} else {
				temp.push(ISSUED_INSTRU.pop());
			}
		}
		while (!temp.isEmpty()) {
			ISSUED_INSTRU.push(temp.pop());
		}
		}
	}

	private static boolean is_BranchInstru(int instru, String[] Inset, int[] goto_instru, int PC, String[] GPR_state,
			int[] GPR, boolean[] is_stall_last) {

		/********************************
		 * int Instru_1_addr ; QUEUE temp = new QUEUE(20) ; while
		 * (!Pre_ISSUE_QUEUE.isEmpty()) { Instru_1_addr = Pre_ISSUE_QUEUE.pop(); // WAW
		 * with issued while(!.isEmpty()) { int instr_temp = ISSUED_INSTRU.pop() ;
		 * temp.push(instr_temp); if (Harzed_WAW(Instru_1_addr, InstruSet, instr_temp))
		 * { return ; } } while(!temp.isEmpty()) { ISSUED_INSTRU.push(temp.pop()); } //
		 * WAW with not issued while(!Issue_temp.isEmpty()) { int instr_temp =
		 * Issue_temp.pop() ; temp.push(instr_temp); if (Harzed_WAW(Instru_1_addr,
		 * InstruSet, instr_temp) || Harzed_WAR(Instru_1_addr, InstruSet, instr_temp) ||
		 * Harzed_WAR(instr_temp, InstruSet, Instru_1_addr)) { return ; } }
		 * while(!temp.isEmpty()) { Issue_temp.push(temp.pop()); }
		 * 
		 * if (is_SW_LW(Instru_1_addr, InstruSet) && !issued_ALU1) {
		 * Pre_ALU1_QUEUE.push(Instru_1_addr); ISSUED_INSTRU.push(Instru_1_addr);
		 * Harzed(Instru_1_addr, InstruSet, GPR_state) ; issued_ALU1 = true; }else if
		 * (!issued_ALU2) { Pre_ALU2_QUEUE.push(Instru_1_addr);
		 * ISSUED_INSTRU.push(Instru_1_addr); Harzed(Instru_1_addr, InstruSet,
		 * GPR_state); issued_ALU2 = true ; }else { Issue_temp.push(Instru_1_addr); }
		 * 
		 * /
		 *******************************/

		// 解析出来指令码及指令类型
		String in_type = Inset[instru].substring(0, 2);
		String in_opcode = Inset[instru].substring(2, 6);
		int rs_6_11 = Integer.parseInt(Inset[instru].substring(6, 11), 2);
		int rt_11_16 = Integer.parseInt(Inset[instru].substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Inset[instru].substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Inset[instru].substring(21, 26), 2);

		int immediate = Integer.parseInt(Inset[instru].substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;
		// J
		int instr_index = Integer.parseInt(Inset[instru].substring(6, 32), 2) << 2;

		if (in_type.equals("01") && (in_opcode.equals("0000") || in_opcode.equals("0001") || in_opcode.equals("0010")
				|| in_opcode.equals("0010") || in_opcode.equals("0011") || in_opcode.equals("0100"))) {
			// 解析需要的操作数
			switch (in_opcode) {
			// J
			case "0000":
				// code = "J " + "#" + instr_index;
				// instr_count = instr_index;
				is_stall_last[0] = false;
				goto_instru[0] = (instr_index - PC) / 4;
				break;
			// JR
			case "0001":
				// code = "JR " + "R" + rs_6_11;
				// instr_count = GPR[rs_6_11];
				if (GPR_state[rs_6_11] != null) {
					is_stall_last[0] = true;
				} else {
					goto_instru[0] = (GPR[rs_6_11] - PC) / 4;
				}
				break;
			// BEQ
			case "0010":
				// code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
				if (GPR_state[rs_6_11] != null || GPR_state[rt_11_16] != null) {
					is_stall_last[0] = true;
				} else if (GPR[rs_6_11] == GPR[rt_11_16]) {
					// instr_count = PC + offset_left2 + 4;
					goto_instru[0] = (offset_left2 + 4) / 4 + instru;
				} else {
					goto_instru[0] = instru + 1;
				}
				break;

			// BLTZ
			case "0011":
				// code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;
				if (GPR_state[rs_6_11] != null) {
					is_stall_last[0] = true;
				} else if (GPR[rs_6_11] < 0) {
					// instr_count = PC + offset_left2 + 4;
					goto_instru[0] = (offset_left2 + 4) / 4 + instru;
				} else {
					goto_instru[0] = instru + 1;
				}
				break;
			// BGTZ
			case "0100":
				// code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;
				if (GPR_state[rs_6_11] != null) {
					is_stall_last[0] = true;
				} else if (GPR[rs_6_11] > 0) {
					// instr_count = PC + offset_left2 + 4;
					goto_instru[0] = (offset_left2 + 4) / 4 + instru;
				} else {
					goto_instru[0] = instru + 1;
				}
				break;
			default:
				break;
			}
			return true;
		} else {
			return false;
		}

	}

	private static boolean is_BREAK(int FetchInstr1, String[] instrSet) {
		// TODO Auto-generated method stub
		String Instru = instrSet[FetchInstr1];

		String in_type = Instru.substring(0, 2);
		String in_opcode = Instru.substring(2, 6);

		if (in_type.equals("01") && (in_opcode.equals("0101") || in_opcode.equals("1011"))) {
			return true;
		}
		return false;
	}

	private static boolean Harzed(int Instr_addr, String[] InstruSet, String[] GPR_state) {
		//

		String Instr_now = InstruSet[Instr_addr];
		// 解析出来指令码及指令类型
		String in_type = Instr_now.substring(0, 2);
		String in_opcode = Instr_now.substring(2, 6);

		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Instr_now.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Instr_now.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Instr_now.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Instr_now.substring(21, 26), 2);

		int immediate = Integer.parseInt(Instr_now.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				if (GPR_state[rt_11_16] != null) {
					return true;
				} else {
					// GPR_state[rt_11_16] = "SW";
					return false;
				}
				// LW
			case "0111":
				// code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				// GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];

				if (GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "LW";

					return false;
				}
				// SLL
			case "1000":
				// code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] << sa;
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SLL";
					return false;
				}
				// SRL
			case "1001":
				// code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SRL";
					return false;
				}
				// SRA
			case "1010":
				// code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				// int xsrl = GPR[rt_11_16] >> sa;
				// int w = 4 << 3;
				// GPR[rd_16_21] |= (-1 << (w - sa));
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SRA";
					return false;
				}
				// NOP
			case "1011":
				// code = "NOP";

				break;
			// default
			default:

				break;
			}

		} else if (in_type.equals("11")) {
			switch (in_opcode) {
			// ADD
			case "0000":
				// code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] + GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "ADD";
					return false;
				}
				// SUB
			case "0001":
				// code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] - GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SUB";
					return false;
				}
				// MUL
			case "0010":
				// code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] * GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "MUL";
					return false;
				}
				// AND
			case "0011":
				// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "AND";
					return false;
				}
				// OR
			case "0100":
				// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "OR";
					return false;
				}
				// XOR
			case "0101":
				// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "XOR";
					return false;
				}
				// NOR
			case "0110":
				// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "NOR";
					return false;
				}
				// SLT
			case "0111":
				// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SLT";
					return false;
				}

				// ADDI
			case "1000":
				// code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// try {
				// GPR[rt_11_16] = GPR[rs_6_11] + immediate;
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }
				if (GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "ADDI";
					return false;
				}

				// ANDI
			case "1001":
				// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rd_16_21] = GPR[rs_6_11] & immediate;
				if (GPR_state[rd_16_21] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "ANDI";
					return false;
				}

				// ORI
			case "1010":
				// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] | immediate;
				if (GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "ORI";
					return false;
				}

				// XORI
			case "1011":
				// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
				if (GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "XORI";
					return false;
				}
				// default
			default:
				break;
			}
		}
		return false;
	}

	private static boolean Harzed_WAR(int Instr_1, String[] InstruSet, int Instr_2) throws Exception {
		String[] reg_state_temp = new String[32];

		String Instr_now = InstruSet[Instr_2];
		// 解析出来指令码及指令类型
		String in_type = Instr_now.substring(0, 2);
		String in_opcode = Instr_now.substring(2, 6);

		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Instr_now.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Instr_now.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Instr_now.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Instr_now.substring(21, 26), 2);

		int immediate = Integer.parseInt(Instr_now.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// reg_state_temp[rt_11_16] = "SW";
				reg_state_temp[base] = "SW";
				break;
			// LW
			case "0111":
				reg_state_temp[base] = "LW";
				// reg_state_temp[rt_11_16] = "LW";
				break;
			// SLL
			case "1000":
				// code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] << sa;

				reg_state_temp[rt_11_16] = "SLL";
				break;
			// SRL
			case "1001":
				// code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				reg_state_temp[rt_11_16] = "SRL";
				break;
			// SRA
			case "1010":
				// code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				// int xsrl = GPR[rt_11_16] >> sa;
				// int w = 4 << 3;
				// GPR[rd_16_21] |= (-1 << (w - sa));

				reg_state_temp[rt_11_16] = "SRA";
				break;
			// NOP
			case "1011":
				// code = "NOP";

				break;
			// default
			default:

				break;
			}

		} else if (in_type.equals("11")) {
			switch (in_opcode) {
			// ADD
			case "0000":
				// code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				reg_state_temp[rt_11_16] = "ADD";
				reg_state_temp[rs_6_11] = "ADD";
				break;
			// SUB
			case "0001":
				// code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				reg_state_temp[rt_11_16] = "SUB";
				reg_state_temp[rs_6_11] = "SUB";
				break;
			// MUL
			case "0010":
				// code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				reg_state_temp[rt_11_16] = "MUL";
				reg_state_temp[rs_6_11] = "MUL";
				break;
			// AND
			case "0011":
				// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];

				reg_state_temp[rd_16_21] = "AND";
				reg_state_temp[rs_6_11] = "AND";
				break;
			// OR
			case "0100":
				// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];

				reg_state_temp[rd_16_21] = "OR";
				reg_state_temp[rs_6_11] = "OR";
				break;
			// XOR
			case "0101":
				// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
				reg_state_temp[rd_16_21] = "XOR";
				reg_state_temp[rs_6_11] = "XOR";
				break;
			// NOR
			case "0110":
				// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);

				reg_state_temp[rd_16_21] = "NOR";
				reg_state_temp[rs_6_11] = "NOR";
				break;
			// SLT
			case "0111":
				// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;

				reg_state_temp[rd_16_21] = "SLT";
				reg_state_temp[rs_6_11] = "SLT";
				break;
			// ADDI
			case "1000":
				// code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;

				reg_state_temp[rs_6_11] = "ADDI";

				break;
			// ANDI
			case "1001":
				// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rd_16_21] = GPR[rs_6_11] & immediate;

				reg_state_temp[rs_6_11] = "ANDI";
				break;
			// ORI
			case "1010":
				// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] | immediate;

				reg_state_temp[rs_6_11] = "ORI";
				break;
			// XORI
			case "1011":
				// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
				reg_state_temp[rs_6_11] = "XORI";
				break;
			// default
			default:
				break;
			}
		}

		return Harzed(Instr_1, InstruSet, reg_state_temp);
	}

	private static boolean Harzed_WAW(int instr_1, String[] InstruSet, int instr_2) throws Exception {
		String[] reg_state_temp = new String[32];

		String Instr_now = InstruSet[instr_2];

		// 解析出来指令码及指令类型
		String in_type = Instr_now.substring(0, 2);
		String in_opcode = Instr_now.substring(2, 6);

		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Instr_now.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Instr_now.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Instr_now.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Instr_now.substring(21, 26), 2);

		int immediate = Integer.parseInt(Instr_now.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				reg_state_temp[rt_11_16] = "SW";
				break;
			// LW
			case "0111":
				// code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				// GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];

				reg_state_temp[rt_11_16] = "LW";
				// reg_state_temp[base] = "LW";
				break;
			// SLL
			case "1000":
				// code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] << sa;
				reg_state_temp[rd_16_21] = "SLL";
				// reg_state_temp[rt_11_16] = "SLL";
				break;
			// SRL
			case "1001":
				// code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				reg_state_temp[rd_16_21] = "SRL";
				// reg_state_temp[rt_11_16] = "SRL";
				break;
			// SRA
			case "1010":
				// code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;

				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				// int xsrl = GPR[rt_11_16] >> sa;
				// int w = 4 << 3;
				// GPR[rd_16_21] |= (-1 << (w - sa));

				reg_state_temp[rd_16_21] = "SRA";
				// reg_state_temp[rt_11_16] = "SRA";
				break;
			// NOP
			case "1011":
				// code = "NOP";

				break;
			// default
			default:

				break;
			}

		} else if (in_type.equals("11")) {
			switch (in_opcode) {
			// ADD
			case "0000":
				// code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;

				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] + GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }

				reg_state_temp[rd_16_21] = "ADD";
				// reg_state_temp[rt_11_16] = "ADD";
				// reg_state_temp[rs_6_11] = "ADD";
				break;

			// SUB
			case "0001":
				// code = "SUB " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] - GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }

				reg_state_temp[rd_16_21] = "SUB";
				// reg_state_temp[rt_11_16] = "SUB";
				// reg_state_temp[rs_6_11] = "SUB";
				break;

			// MUL
			case "0010":
				// code = "MUL " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// try {
				// GPR[rd_16_21] = GPR[rs_6_11] * GPR[rt_11_16];
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }

				reg_state_temp[rd_16_21] = "MUL";
				// reg_state_temp[rt_11_16] = "MUL";
				// reg_state_temp[rs_6_11] = "MUL";
				break;
			// AND
			case "0011":
				// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];

				reg_state_temp[rd_16_21] = "AND";
				// reg_state_temp[rs_6_11] = "AND";
				break;
			// OR
			case "0100":
				// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];

				reg_state_temp[rd_16_21] = "OR";
				// reg_state_temp[rs_6_11] = "OR";
				break;
			// XOR
			case "0101":
				// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];

				reg_state_temp[rd_16_21] = "XOR";
				// reg_state_temp[rs_6_11] = "XOR";
				break;
			// NOR
			case "0110":
				// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);

				reg_state_temp[rd_16_21] = "NOR";
				// reg_state_temp[rs_6_11] = "NOR";
				break;
			// SLT
			case "0111":
				// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;

				reg_state_temp[rd_16_21] = "SLT";
				// reg_state_temp[rs_6_11] = "SLT";
				break;
			// ADDI
			case "1000":
				// code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// try {
				// GPR[rt_11_16] = GPR[rs_6_11] + immediate;
				// } catch (Exception e) {
				// // TODO: handle exception
				// System.out.println("SignalException(IntegerOverflow)");
				// }

				reg_state_temp[rt_11_16] = "ADDI";
				// reg_state_temp[rs_6_11] = "ADDI";
				break;
			// ANDI
			case "1001":
				// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rd_16_21] = GPR[rs_6_11] & immediate;

				reg_state_temp[rd_16_21] = "ANDI";
				// reg_state_temp[rs_6_11] = "ANDI";
				break;
			// ORI
			case "1010":
				// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] | immediate;

				reg_state_temp[rt_11_16] = "ORI";
				// reg_state_temp[rs_6_11] = "ORI";
				break;
			// XORI
			case "1011":
				// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;

				reg_state_temp[rt_11_16] = "XORI";
				// reg_state_temp[rs_6_11] = "XORI";
				break;
			// default
			default:
				break;
			}
		}
		return Harzed(instr_1, InstruSet, reg_state_temp);
	}

	private static boolean is_SW_LW(int Instr_addr, String[] InstruSet) {
		String Instr_now = InstruSet[Instr_addr];
		// 解析出来指令码及指令类型
		String in_type = Instr_now.substring(0, 2);
		String in_opcode = Instr_now.substring(2, 6);

		// 解析需要的操作数
		int rs_6_11 = Integer.parseInt(Instr_now.substring(6, 11), 2);
		int base = rs_6_11;
		int rt_11_16 = Integer.parseInt(Instr_now.substring(11, 16), 2);
		int rd_16_21 = Integer.parseInt(Instr_now.substring(16, 21), 2);
		// SLL sa
		int sa = Integer.parseInt(Instr_now.substring(21, 26), 2);

		int immediate = Integer.parseInt(Instr_now.substring(16, 32), 2);
		int offset = immediate;
		int offset_left2 = offset << 2;

		if (in_type.equals("01")) {
			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				return true;
			// LW
			case "0111":
				// code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
				// GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}

	}

	private static String[] decode(String[] Inst, int[] Data, int PC, int Data_address) {
		int len = Inst.length;
		String[] string_result = new String[len];
		int address = 256;
		// int Data_address = 0 ;
		int Data_count = 0;
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
						Data_address = address + 4;
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
			// reach to the Data code
			else {
				String inst_and_add = Inst[i] + "\t" + address + "\t";
				int Data_temp;
				int symbol = Integer.parseInt(Inst[i].substring(0, 1));
				if (symbol == 0) {
					Data_temp = (Integer.parseInt(Inst[i].substring(0, 32), 2));
				} else {
					Data_temp = (Integer.parseInt(Inst[i].substring(1, 32), 2) - Integer.MAX_VALUE - 1);
				}

				string_result[i] = inst_and_add + Data_temp;
				Data[Data_count] = Data_temp;

				Data_count++;
				address += 4;
			}
		}
		return string_result;
	}

	private static String[] readtxt(String input_path) {
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

	private static void ShowResult(String[] GPR_state, int[] GPR, int[] Data) {
	/*	// show reg_state
		System.out.println();
		System.out.println("GPR_state");

		for (int i = 0; i < GPR_state.length; i++) {
			if (i % 8 == 0) {
				System.out.println("");
			}
			System.out.print(GPR_state[i] + "\t");
		}
		*/
		// show reg_state
		System.out.println();
		System.out.println("GPR");
		for (int i = 0; i < GPR.length; i++) {
			if (i % 8 == 0) {
				System.out.println("");
			}
			System.out.print(GPR[i] + "\t");
		}

		// show Data
		System.out.println("Data");
		for (int i = 0; i < Data.length; i++) {
			if (i % 8 == 0) {
				System.out.println("");
			}
			System.out.print(Data[i] + "\t");
		}
	}

	private static void ShowQueues(String[] deassems, QUEUE waiting_QUEUE, QUEUE executed_QUEUE, QUEUE pre_ISSUE_QUEUE,
			QUEUE pre_ALU1_QUEUE, QUEUE pre_ALU2_QUEUE, QUEUE pre_MEM_QUEUE, QUEUE post_MEM_QUEUE,
			QUEUE post_ALU2_QUEUE) throws Exception {
		/***********************/
		QUEUE show_temp = new QUEUE(4);
		System.out.println("IF Unit:");

		System.out.println("Waiting Que:");
		while (!waiting_QUEUE.isEmpty()) {
			System.out.println(deassems[waiting_QUEUE.peek()]);
			show_temp.push(waiting_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			waiting_QUEUE.push(show_temp.pop());
		}

		System.out.println("Executed Que:");
		while (!executed_QUEUE.isEmpty()) {
			System.out.println(deassems[executed_QUEUE.peek()]);
			show_temp.push(executed_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			executed_QUEUE.push(show_temp.pop());
		}

		System.out.println("Pre-Issue Que:");
		while (!pre_ISSUE_QUEUE.isEmpty()) {
			System.out.println(deassems[pre_ISSUE_QUEUE.peek()]);
			show_temp.push(pre_ISSUE_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			pre_ISSUE_QUEUE.push(show_temp.pop());
		}

		System.out.println("Pre-ALU1 Que");
		while (!pre_ALU1_QUEUE.isEmpty()) {
			System.out.println(deassems[pre_ALU1_QUEUE.peek()]);
			show_temp.push(pre_ALU1_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			pre_ALU1_QUEUE.push(show_temp.pop());
		}

		System.out.println("Pre-MEM Que:");
		while (!pre_MEM_QUEUE.isEmpty()) {
			System.out.println(deassems[pre_MEM_QUEUE.peek()]);
			show_temp.push(pre_MEM_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			pre_MEM_QUEUE.push(show_temp.pop());
		}

		System.out.println("Post-MEM Que:");
		while (!post_MEM_QUEUE.isEmpty()) {
			System.out.println(deassems[post_MEM_QUEUE.peek()]);
			show_temp.push(post_MEM_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			post_MEM_QUEUE.push(show_temp.pop());
		}

		System.out.println("Pre-ALU2 Que:");
		while (!pre_ALU2_QUEUE.isEmpty()) {
			System.out.println(deassems[pre_ALU2_QUEUE.peek()]);
			show_temp.push(pre_ALU2_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			pre_ALU2_QUEUE.push(show_temp.pop());
		}

		System.out.println("Post-ALU2 Que:");
		while (!post_ALU2_QUEUE.isEmpty()) {
			System.out.println(deassems[post_ALU2_QUEUE.peek()]);
			show_temp.push(post_ALU2_QUEUE.pop());
		}
		while (show_temp != null && !show_temp.isEmpty()) {
			post_ALU2_QUEUE.push(show_temp.pop());
		}

	}
}
