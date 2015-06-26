/*
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License V3.0 for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 Copyright (C) 2015, Bahar Sateli
*/
package com.baharsateli.lod.convertors;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * A lightweight class for exporting Jena TDB triple-store to various formats.
 * 
 * @author Bahar Sateli
 */
public class TDBExporter {

	/**
	 * Available serialization formats according to Jena javadoc.
	 * 
	 * @see https
	 *      ://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/rdf
	 *      /model/Model.html#write(java.io.OutputStream,%20java.lang.String)
	 */
	private enum Formats {
		XML("RDF/XML", "rdf"), XMLA("RDF/XML-ABBREV", "rdf"), TTL("TURTLE","ttl"),
		NT("N-TRIPLE", "n3"), N3("N3", "n3"), CSV("CSV", "csv");

		private final String label;
		private final String extension;

		private Formats(String label, String extension) {
			this.label = label;
			this.extension = extension;
		}

		@Override
		public String toString() {
			return label;
		}
		
		static public boolean isMember(final String formatName) {
			Formats[] values = Formats.values();
		       for (Formats value : values)
		           if (value.name().equals(formatName.toUpperCase()))
		               return true;
		       return false;
		}
	}

	private static Dataset dataset = null;
	private static FileWriter writer = null;
	private static String outputPath = null;

	public static void main(String[] args) {

		if (args.length > 0 && args.length < 3) {
			String outputFormat = args[0];
			String datasetPath = args[1];
			if (sanityCheck(args[0], args[1])) {
				try {
					dataset = TDBFactory.createDataset(datasetPath);
					Model model = dataset.getDefaultModel();
					
					// output file written in the same directory as the execution
					outputPath = System.getProperty("user.dir")
							+ System.getProperty("file.separator")
							+ "dump."
							+ Formats.valueOf(outputFormat).extension;

					switch (Formats.valueOf(outputFormat).label) {
						case "RDF/XML":
						case "RDF/XML-ABBREV":
						case "TURTLE":
						case "N-TRIPLE":
						case "N3": {
							writer = new FileWriter(outputPath);
							model.write(writer, Formats.valueOf(outputFormat).label);
							System.out.println("[INFO] Output file written to " + outputPath);
							break;
						}
						case "CSV": {
							dataset.begin(ReadWrite.READ);
							String query = "SELECT * WHERE {?s ?p ?o}";
							QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
							ResultSet rs = qExec.execSelect();
							try {
								OutputStream outputStream = new FileOutputStream(outputPath);
								ResultSetFormatter.outputAsCSV(outputStream, rs);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							dataset.close();
							System.out.println("[INFO] Output file written to " + outputPath);
							break;
						}
						default: {
							System.out.println("Unsupported output format. Code should not get here!");
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					// prevent resource leakage
					dataset.close();
				}
			}else{
				System.out.println("[INFO] Execution aborted.");
			}
		} else {
			System.out.println("[ERROR] No arguments provided. \nExecution aborted.");
		}
	}

	/**
	 * This method checks whether the user-provided arguments are acceptable for
	 * further processing.
	 * 
	 * @param format
	 *            the output format (should be one of the eNums in this class)
	 * @param tdbPath
	 *            the absolute path to the directory containing TDB files
	 * @return
	 */
	private static boolean sanityCheck(final String format, final String tdbPath) {
		// first check if we support the output format
		if(Formats.isMember(format)){
			
			// then check if the TDB directory exists and is readable
			Path path = FileSystems.getDefault().getPath(tdbPath);
			
			// we do not handle symbolic links to the TDB directory
			if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
				if(Files.isReadable(path)){
					return true;
				}else{
					System.out.println("[ERROR] The provided TDB directory is not readable. Please change the directory permissions and try again.");
				}
			}else{
				System.out.println("[ERROR] No TDB directory found on " + tdbPath);
			}
		}else{
			System.out.println("[ERROR] Unsupported output format: " + format);
		}
		return false;
	}
}
