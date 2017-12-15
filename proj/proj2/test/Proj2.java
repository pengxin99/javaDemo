import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Stack;

import javax.swing.text.StyledEditorKit.BoldAction;

import com.sun.beans.util.Cache;
import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.istack.internal.FragmentContentHandler;
import com.sun.javafx.stage.FocusUngrabEvent;
import com.sun.org.apache.regexp.internal.REUtil;
import com.sun.org.apache.xpath.internal.functions.FuncStartsWith;

import javafx.beans.binding.When;
import sun.security.jca.GetInstance.Instance;

class FuncUnit {
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

	public FuncUnit(String name, boolean busy, String op, String fi, String fj, String fk, String qj, String qk,
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

class Que {
	private int[] objs;
	private int head;
	private int end;
	private int size;

	//
	public Que(int size) {
		objs = new int[size];
		this.head = 0;
		this.end = -1;
		this.size = 0;
	}

	public void push(int instr_count) throws Exception {
		if (this.size > objs.length)
			throw new Exception("Que is full!");
		if (end == objs.length - 1)
			end = -1;
		objs[++end] = instr_count;
		size++;
	}

	public int pop() throws Exception {
		if (this.size == 0)
			throw new Exception("Que is empty!");
		int tmp = objs[head++];
		if (head == objs.length)
			head = 0;
		size--;
		return tmp;
	}

	public int peek() throws Exception {
		if (this.size == 0)
			throw new Exception("Que is empty!");
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

public class Proj2 {
	public static void main(String[] args) throws Exception {

		FuncUnit IF = new FuncUnit(null, false, null, null, null, null, null, null, false, false);
		FuncUnit ISSUE = new FuncUnit(null, false, null, null, null, null, null, null, false, false);
		FuncUnit ALU1 = new FuncUnit(null, false, null, null, null, null, null, null, false, false);
		FuncUnit ALU2 = new FuncUnit(null, false, null, null, null, null, null, null, false, false);
		FuncUnit MEM = new FuncUnit(null, false, null, null, null, null, null, null, false, false);
		FuncUnit WB = new FuncUnit(null, false, null, null, null, null, null, null, false, false);

		FuncUnit[] FuncUnit_State = new FuncUnit[6];
		FuncUnit_State[0] = IF;
		FuncUnit_State[1] = ISSUE;
		FuncUnit_State[2] = ALU1;
		FuncUnit_State[3] = ALU2;
		FuncUnit_State[4] = MEM;
		FuncUnit_State[5] = WB;

		Que Pre_issue_Que = new Que(4);
		Que Pre_ALU1_Que = new Que(2);
		Que Pre_ALU2_Que = new Que(2);
		Que Pre_MEM_Que = new Que(1);
		Que Post_ALU2_Que = new Que(1);
		Que Post_MEM_Que = new Que(1);

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
		int[] MEM_cache = null;
		boolean is_stall_last = false;

		while (is_go) {
			System.out.println("cycle:" + Cycle);

			WriteBack(ALU1_cache, MEM_cache, GPR, data, FuncUnit_State, GPR_state);

			MEM_cache = MEM(Pre_MEM_Que, Post_MEM_Que, instr_count, InSet, data, GPR, data_address);

			ALU1_cache = ALU1(InSet, Pre_ALU1_Que, Pre_MEM_Que, data, GPR, data_address);

			ALU2_cache = ALU2(InSet, Pre_ALU2_Que, GPR, data);

			Exe_ISSUE(InSet, FuncUnit_State, Pre_issue_Que, Pre_ALU1_Que, Pre_ALU2_Que, GPR_state);

			instr_count = Execute_IF(deassems, Pre_issue_Que, is_stall_last, instr_count, GPR_state, GPR, PC);
			//
			if (Cycle > 30) {
				is_go = false;
			}

			/***********************/
			System.out.println("IF Unit:");
			System.out.println("Pre-Issue Que:");
			if (!Pre_issue_Que.isEmpty()) {
				System.out.println(deassems[Pre_issue_Que.peek()]);
			}
			System.out.println("Pre-ALU1 Que");
			if (!Pre_ALU1_Que.isEmpty()) {
				System.out.println(deassems[Pre_ALU1_Que.peek()]);
			}
			System.out.println("Pre-ALU2 Que:");
			while (!Pre_ALU2_Que.isEmpty()) {
				System.out.println(deassems[Pre_ALU2_Que.pop()]);
			}

			// show reg
			System.out.println("GPR");
			/*
			 * for (int i = 0; i < GPR.length; i++) { if (i % 8 == 0) {
			 * System.out.println(""); } System.out.print(GPR[i] + "\t"); } // show data
			 * System.out.println("Data"); for (int i = 0; i < data.length; i++) { if (i % 8
			 * == 0) { System.out.println(""); } System.out.print(data[i] + "\t"); }
			 */

			for (int i = 0; i < GPR.length; i++) {
				if (i % 8 == 0) {
					System.out.println("");
				}
				System.out.print(GPR[i] + "\t");
			}
			// show GPR_state
			for (int i = 0; i < GPR_state.length; i++) {
				if (i % 8 == 0) {
					System.out.println("");
				}
				System.out.print(GPR_state[i] + "\t");
			}
			Cycle++;
		}
	}

	private static int Execute_IF(String[] InstructionSet, Que Pre_issue_Que, boolean is_stall_last, int instr_count,
			String[] GPR_state, int[] GPR, int PC) throws Exception {
		// TODO Auto-generated method stub

		String WaitingInstr;
		String ExecutedInstr;
		int Instru_now;
		int Instru_2;
		int goto_add = 0;

		if (is_stall_last || Pre_issue_Que.isFull()) {
			return instr_count;
			// return instr_count;
		} else {
			// fetch instr one
			Instru_now = instr_count++;

			if (is_BranchInstru(Instru_now, InstructionSet, goto_add, PC, GPR_state, GPR, is_stall_last)) {
				if (Harzed(Instru_now, InstructionSet, GPR_state, false)) {
					is_stall_last = true;
				} else {
					Instru_now = goto_add;
				}
			} else if (Harzed(Instru_now, InstructionSet, GPR_state, false)) {
				Pre_issue_Que.push(Instru_now);
				// fetch instru two
				Instru_now = instr_count++;
				if (is_BranchInstru(Instru_now, InstructionSet, goto_add, PC, GPR_state, GPR, is_stall_last)) {
					if (Harzed(Instru_now, InstructionSet, GPR_state, false)) {
						is_stall_last = true;
					} else {
						Instru_now = goto_add;
					}
				}
				Pre_issue_Que.push(Instru_now);
			}
		}
		return instr_count;
	}

	private static void Exe_ISSUE(String[] InstructionSet, FuncUnit[] FuncUnit_State, Que Pre_issue_Que,
			Que Pre_ALU1_Que, Que Pre_ALU2_Que, String[] GPR_state) throws Exception {

		if (Pre_issue_Que.isEmpty()) {
			return;
		}

		boolean issued_ALU1 = false;
		boolean issued_ALU2 = false;
		// temp queue for instru
		Que temp_que = new Que(4);

		while (!Pre_issue_Que.isEmpty()) {
			int Instru_now_addr = Pre_issue_Que.pop();
			boolean is_SW_LW = false;
			if (!Harzed(Instru_now_addr, InstructionSet, GPR_state, is_SW_LW)) {
				if (is_SW_LW) {
					Pre_ALU1_Que.push(Instru_now_addr);
				} else {
					Pre_ALU2_Que.push(Instru_now_addr);
				}
			} else {
				temp_que.push(Instru_now_addr);
			}
		}
		while (!temp_que.isEmpty()) {
			Pre_issue_Que.push(temp_que.pop());
		}
	}

	private static boolean is_BranchInstru(int instru, String[] Inset, int instr_count, int PC, String[] GPR_state,
			int[] GPR, boolean is_stall_last) {
		// TODO Auto-generated method stub
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
				instr_count = (instr_index - PC) / 4;
				break;
			// JR
			case "0001":
				// code = "JR " + "R" + rs_6_11;
				// instr_count = GPR[rs_6_11];
				instr_count = (GPR[rs_6_11] - PC) / 4;

				break;
			// BEQ
			case "0010":
				// code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
				if (GPR_state[rs_6_11] != null || GPR_state[rt_11_16] != null) {
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
				if (GPR_state[rs_6_11] != null) {
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
				if (GPR_state[rs_6_11] != null) {
					is_stall_last = true;
				} else {
					if (GPR[rs_6_11] > 0)
						// instr_count = PC + offset_left2 + 4;
						instr_count = (offset_left2 + 4) / 4;
				}
				break;
			}

			return true;
		} else {
			return false;
		}

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

	private static boolean Harzed(int Instr_addr, String[] InstruSet, String[] GPR_state, boolean is_SW_LW) {
		//
		String Instr_now = InstruSet[Instr_addr];
		// 解析出来指令码及指令类型
		String in_type = Instr_now.substring(0, 2);
		String in_opcode = Instr_now.substring(2, 6);

		is_SW_LW = false;
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
				is_SW_LW = true;
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
				is_SW_LW = true;
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
				if (GPR_state[rd_16_21] != null && GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "SLL";
					return false;
				}
				// SRL
			case "1001":
				// code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
				// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
				if (GPR_state[rd_16_21] != null && GPR_state[rt_11_16] != null) {
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
				if (GPR_state[rd_16_21] != null || GPR_state[rt_11_16] != null) {
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
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null || GPR_state[rt_11_16] != null) {
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
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null || GPR_state[rt_11_16] != null) {
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
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null || GPR_state[rt_11_16] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "MUL";
					return false;
				}
				// AND
			case "0011":
				// code = "AND " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "AND";
					return false;
				}
				// OR
			case "0100":
				// code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "OR";
					return false;
				}
				// XOR
			case "0101":
				// code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "XOR";
					return false;
				}
				// NOR
			case "0110":
				// code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "NOR";
					return false;
				}
				// SLT
			case "0111":
				// code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
				// GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
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
				if (GPR_state[rt_11_16] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "ADDI";
					return false;
				}

				// ANDI
			case "1001":
				// code = "ANDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rd_16_21] = GPR[rs_6_11] & immediate;
				if (GPR_state[rd_16_21] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rd_16_21] = "ANDI";
					return false;
				}

				// ORI
			case "1010":
				// code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] | immediate;
				if (GPR_state[rt_11_16] != null || GPR_state[rs_6_11] != null) {
					return true;
				} else {
					GPR_state[rt_11_16] = "ORI";
					return false;
				}

				// XORI
			case "1011":
				// code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
				// GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
				if (GPR_state[rt_11_16] != null || GPR_state[rs_6_11] != null) {
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

	private static int[] ALU1(String[] InstruSet, Que Pre_ALU1_Que, Que pre_Mem, int[] data, int[] GPR,
			int data_address) throws Exception {

		if (Pre_ALU1_Que.isEmpty()) {
			return null;
		}

		String code = null;

		int[] cache = new int[2];
		int Inst_addr = Pre_ALU1_Que.pop();
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

	private static int[] ALU2(String[] InstruSet, Que Pre_ALU2_Que, int[] GPR, int[] data) throws Exception {
		if (Pre_ALU2_Que.isEmpty()) {
			return null;
		}

		int Inst_addr = Pre_ALU2_Que.pop();
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

	private static int[] MEM(Que Pre_MEM, Que Post_MEM, int instr_count, String[] InstruSet, int[] data, int[] GPR,
			int data_address) {
		if (Pre_MEM.isEmpty()) {
			return null;
		} else {
			String Instru_now = InstruSet[instr_count];
			int[] cache = null;

			String in_type = Instru_now.substring(0, 2);
			String in_opcode = Instru_now.substring(2, 6);

			int rs_6_11 = Integer.parseInt(Instru_now.substring(6, 11), 2);
			int base = rs_6_11;
			int rt_11_16 = Integer.parseInt(Instru_now.substring(11, 16), 2);
			int rd_16_21 = Integer.parseInt(Instru_now.substring(16, 21), 2);
			// SLL sa
			int sa = Integer.parseInt(Instru_now.substring(21, 26), 2);

			int immediate = Integer.parseInt(Instru_now.substring(16, 32), 2);
			int offset = immediate;
			int offset_left2 = offset << 2;
			// J target
			int instr_index = Integer.parseInt(Instru_now.substring(6, 32), 2) << 2;

			switch (in_opcode) {
			// SW
			case "0110":
				// code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				data[(offset - data_address + GPR[base]) / 4] = GPR[rt_11_16];
				break;
			// LW
			case "0111":
				// code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";

				// GPR[rt_11_16] = data[(offset - data_address + GPR[base]) / 4];
				cache[0] = rt_11_16;
				cache[1] = data[(offset - data_address + GPR[base]) / 4];
				break;

			default:
				break;
			}
			return cache;

		}
	}

	private static void WriteBack(int[] MEM_cache, int[] ALU2_cache, int[] reg, int[] Data, FuncUnit[] FucS,
			String[] GPR_state) {
		if (MEM_cache == null) {
			System.out.println("ALU1 write null");
		} else {
			int Data_des = MEM_cache[0];
			int Data_data = MEM_cache[1];
			Data[Data_des] = Data_data;
		}
		if (ALU2_cache == null) {
			System.out.println("ALU2 write null");
		} else {
			int PRG_des = ALU2_cache[0];
			int PRG_data = ALU2_cache[1];

			// test
			reg[PRG_des] = PRG_data;
			// release the register
			GPR_state[PRG_des] = null;
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
