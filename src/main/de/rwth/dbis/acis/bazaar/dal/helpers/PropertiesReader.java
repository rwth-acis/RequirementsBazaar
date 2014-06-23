/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package de.rwth.dbis.acis.bazaar.dal.helpers;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/22/2014
 */
public class PropertiesReader {

    //TODO Attempt #1 failed
    public Properties read(String fileName) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;
//        input =  this.getClass().getClassLoader().getResourceAsStream(fileName);
        URL resource = this.getClass().getClassLoader().getResource(".");
        input = resource.openStream();
        if (input == null) {
            throw new FileNotFoundException("Unable to find file: " + fileName);
        }

        prop.load(input);

        return prop;
    }
}
