
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class disassemble {
	public static void main(String[] args) {
		// to define the path
		String input_path = args[0] ;
		//String input_path = "/home/xinye/桌面/proj/proj1/sample.txt";
		// read file
		String[] InSet = readtxt(input_path);
		// define register and memory
		int[] GPR = new int[32];
		int[] data = new int[24];
		// define the initial PC num
		int PC = 256;

		// decode,and return disassemble Strings
		String[] deassems = decode(InSet, data, PC);
		// Simulation and will change the register and memeory
		// and will write the number to file every Cycle
		GPR_and_Data(InSet, GPR, data, PC, "simulation.txt");

		show(deassems, null);
		// writer the deassems back to file
		WriteID("disassembly.txt", deassems);
	}
	/**
	 * 
	* @Title: readtxt
	* @Description: 读取文件，将二进制待解码的机器码txt文件读入
	* @param input_path，文件路径
	* @return String[] ，instruments，按行将待解码的文件放入字符串数组 instruments中
	* @throws
	* @date 2017年11月17日 下午8:20:35
	 */
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

	/**
	 * 
	* @Title: show
	* @Description: 输出从文件读入的指令集用，检验正误
	* @param Instruments：输入解码后的汇编指令集
	* @param data ：内存数据区，全局变量
	* @return void   
	* @date 2017年11月17日 下午8:25:33
	 */
	public static void show(String[] Instruments, int[] data) {
		for (int i = 0; Instruments[i] != null; i++) {
			System.out.println(Instruments[i]);
		}
	}

	/**
	 * 
	* @Title: decode
	* @Description: 对二进制的机器指令集进行解码
	* @param Inst：从文件读入的汇编指令，每个字符串保存读入的一行指令
	* @param data：内存中的数据区，从汇编码中读入初始值 
	* @param PC：传入第一条指令的执行地址 
	* @return String[] ：二进制机器码码以及对应的解码后的汇编指令，BREAK指令后面为初始化内存数据
	* @date 2017年11月17日 下午8:26:30
	 */
	public static String[] decode(String[] Inst, int[] data, int PC) {
		int len = Inst.length;
		String[] string_result = new String[len];
		int address = 256;
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
	
	/**
	 * 
	* @Title: GPR_and_Data
	* @Description: 对指令执行过程进行仿真，并输出每条指令操作后的寄存器以及内存数据值，输入结果直接写入文件
	* @param Inst：二进制码指令集
	* @param GPR：寄存器数组，全局变量
	* @param data：内存数据数组，全局变量
	* @param PC：初始的指令地址，程序执行后为当前指令地址
	* @param OutPutFile ：需要输出的文件路径
	* @return void   
	* @date 2017年11月17日 下午8:32:01
	 */
	private static void GPR_and_Data(String[] Inst, int[] GPR, int[] data, int PC, String OutPutFile) {
		// TODO Auto-generated method stub
		int PC_now = PC;
		int len = Inst.length;
		String[] string_result = new String[len];
		int PC_base = PC;
		boolean is_break = false;
		int cycle = 1;
		for (int i = 0; !is_break; i = ((PC_now - PC_base) / 4)) {
			System.out.println("--------------------");
			System.out.print("Cycle:" + cycle + "\t" + PC_now + "\t");
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
						System.out.println(code);
						PC_now = instr_index;
						break;
					// JR
					case "0001":
						code = "JR " + "R" + rs_6_11;
						System.out.println(code);
						PC_now = GPR[rs_6_11];
						break;
					// BEQ
					case "0010":
						code = "BEQ " + "R" + rs_6_11 + ", R" + rt_11_16 + ", #" + offset_left2;
						System.out.println(code);
						if (GPR[rs_6_11] == GPR[rt_11_16])
							PC_now = PC + offset_left2 + 4;
						break;
					// BLTZ
					case "0011":
						code = "BLTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						System.out.println(code);
						if (GPR[rs_6_11] < 0)
							PC_now = PC + offset_left2 + 4;
						break;
					// BGTZ
					case "0100":
						code = "BGTZ " + "R" + rs_6_11 + ", #" + offset_left2;
						System.out.println(code);
						if (GPR[rs_6_11] > 0)
							PC_now = PC + offset_left2 + 4;
						break;
					// BREAK
					case "0101":
						code = "BREAK";
						System.out.println(code);
						is_break = true;
						break;
					// SW
					case "0110":
						code = "SW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
						System.out.println(code);
						data[(offset - 340 + GPR[base]) / 4] = GPR[rt_11_16];
						break;
					// LW
					case "0111":
						code = "LW " + "R" + rt_11_16 + ", " + offset + "(R" + base + ")";
						System.out.println(code);
						GPR[rt_11_16] = data[(offset - 340 + GPR[base]) / 4];
						break;
					// SLL
					case "1000":
						code = "SLL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						System.out.println(code);
						GPR[rd_16_21] = GPR[rt_11_16] << sa;
						break;
					// SRL
					case "1001":
						code = "SRL " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						System.out.println(code);
						GPR[rd_16_21] = GPR[rt_11_16] >> sa;
						break;
					// SRA
					case "1010":
						code = "SRA " + "R" + rd_16_21 + ", R" + rt_11_16 + ", #" + sa;
						System.out.println(code);
						// GPR[rd_16_21] = GPR[rt_11_16] >> sa;
						int xsrl = GPR[rt_11_16] >> sa;
						int w = 4 << 3;
						GPR[rd_16_21] |= (-1 << (w - sa));
						break;
					// NOP
					case "1011":
						code = "NOP";
						System.out.println("NOP");
						break;
					// default
					default:
						System.out.println("NULLL!!");
						break;
					}

				} else if (in_type.equals("11")) {
					switch (in_opcode) {
					// ADD
					case "0000":
						code = "ADD " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						System.out.println(code);
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
						System.out.println(code);
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
						System.out.println(code);
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
						System.out.println(code);
						GPR[rd_16_21] = GPR[rs_6_11] & GPR[rd_16_21];
						break;
					// OR
					case "0100":
						code = "OR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						System.out.println(code);
						GPR[rd_16_21] = GPR[rs_6_11] | GPR[rd_16_21];
						break;
					// XOR
					case "0101":
						code = "XOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						System.out.println(code);
						GPR[rd_16_21] = GPR[rs_6_11] ^ GPR[rd_16_21];
						break;
					// NOR
					case "0110":
						code = "NOR " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						System.out.println(code);
						GPR[rd_16_21] = ~(GPR[rs_6_11] | GPR[rd_16_21]);
						break;
					// SLT
					case "0111":
						code = "SLT " + "R" + rd_16_21 + ", R" + rs_6_11 + ", R" + rt_11_16;
						System.out.println(code);
						GPR[rd_16_21] = (GPR[rs_6_11] < GPR[rd_16_21]) ? 1 : 0;
						break;
					// ADDI
					case "1000":
						code = "ADDI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						System.out.println(code);
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
						System.out.println(code);
						GPR[rd_16_21] = GPR[rs_6_11] & immediate;
						break;
					// ORI
					case "1010":
						code = "ORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						System.out.println(code);
						GPR[rt_11_16] = GPR[rs_6_11] | immediate;
						break;
					// XORI
					case "1011":
						code = "XORI " + "R" + rt_11_16 + ", R" + rs_6_11 + ", #" + immediate;
						System.out.println(code);
						GPR[rt_11_16] = GPR[rs_6_11] ^ immediate;
						break;
					// default
					default:
						System.out.println("NULLL!!");
						break;
					}
				}
				// PC is the instrument`s pc,so it is PC ,not PC_now
				temp = "--------------------" + '\n' + "Cycle:" + cycle + "\t" + PC + "\t" + code;
				WriteSimulation(OutPutFile, GPR, temp + '\n' + '\n' + "Registers", "r");
				WriteSimulation(OutPutFile, data, "Data", "d");

			} else {
				System.out.println("break!");
				break;
			}

			PC_now = (PC_now == PC) ? (PC_now + 4) : PC_now;
			PC = PC_now;
			cycle++;
		}
		System.out.println("***************ENDEND************");
	}
	
	/**
	 * 
	* @Title: WriteID
	* @Description: 将解码后的汇编指令写入制定文件
	* @param fileName：需要写入的文件路径及文件名，字符串
	* @param deassems ：解码后的汇编指令集
	* @return void
	* @date 2017年11月17日 下午8:35:20
	 */
	public static void WriteID(String fileName, String[] deassems) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			int i = 0;
			while (deassems[i] != null) {
				writer.write(deassems[i]);
				writer.write('\n');
				i++;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	* @Title: WriteSimulation
	* @Description: 将寄存器和内存数据写入文件，供程序执行仿真时使用
	* @param @param fileName：需要写入数据的文件路径及名称
	* @param @param mem：内存数据区或者寄存器数据区
	* @param @param temp：按要求，写入的指令名称及地址
	* @param @param type ：本次写入数据时，写寄存器还是内存
	* @return void   
	* @date 2017年11月17日 下午8:37:38
	 */
	public static void WriteSimulation(String fileName, int[] mem, String temp, String type) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(temp);
			writer.write('\n');
			for (int i = 0; i < mem.length; i++) {

				if (i % 8 == 0 && i < mem.length - 1) {
					if (type.equals("r")) {
						if (i < 10) {
							writer.write("R0" + i + ":");
						} else {
							writer.write("R" + i + ":");
						}
					} else {
						writer.write(340 + i * 4 + ":");
					}
				}
				writer.write('\t' + String.valueOf(mem[i]));
				if ((i + 1) % 8 == 0 && i < mem.length - 1) {
					writer.write('\n');
				}
//				System.out.println("write lines: " + i);
			}
			writer.write("\n\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

