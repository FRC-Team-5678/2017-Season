using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ConvertLog
{
   

    class Program
    {
        

        static void Main(string[] args)
        {

            string[] labels = { "time", "loopCounter", "checksum", "calculated checksum", "pixyX", "pixyY", "pixyWidth", "pixyHeight", "pixySignature", "leftEncoder", "rightEncoder", "gyro"};

            string line;
            char[] sep = {','};
            string[] substrings;
            string lastStringIndex;
            string name;
            string value;
            string stringIndex;
            

            // Read the file and display it line by line.
            System.IO.StreamReader fileIn =
               new System.IO.StreamReader(@"c:\\users\rosly_000\SmartDashboard\csv.txt");
            System.IO.StreamWriter fileOut = 
                new System.IO.StreamWriter(@"c:\\users\rosly_000\SmartDashboard\formattedData.txt");
            lastStringIndex = "0";
            stringIndex = "0";
            fileIn.ReadLine();  //toss out first line
            Dictionary<string, string> thisData = new Dictionary<string, string>();
            while((line = fileIn.ReadLine()) != null){
                while (line.Split(sep)[0] == lastStringIndex)
                {
                    //Console.WriteLine(line);
                    //counter++;
                    substrings = line.Split(sep);
                    stringIndex = substrings[0];
                    name = substrings[1];
                    value = substrings[2];
                    thisData.Add(name, value);
                }


                lastStringIndex = line.Split(sep)[0];
                string stringout = lastStringIndex + ",";
                string dictValue;
                foreach (string lab in labels)
                {
                    if (thisData.TryGetValue(lab, out dictValue))
                    {
                        stringout += dictValue;
                        thisData.Remove(lab);
                    }
                    else
                    {
                        stringout += "";
                    }
                    stringout += ";";
                }
                fileOut.WriteLine(stringout);
                
                
            }


            fileIn.Close();
            fileOut.Close();
        }
    }
}
