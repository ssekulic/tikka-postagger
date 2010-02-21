///////////////////////////////////////////////////////////////////////////////
//  Copyright (C) 2010 Taesun Moon, The University of Texas at Austin
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 3 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////
package tikka.bhmm.model.m1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tikka.bhmm.model.base.BHMM;
import tikka.bhmm.apps.CommandLineOptions;
import tikka.opennlp.io.DataFormatEnum;
import tikka.opennlp.io.DataReader;
import tikka.opennlp.io.DirReader;
import tikka.structures.StringDoublePair;
import tikka.utils.ec.util.MersenneTwisterFast;
import tikka.utils.normalizer.WordNormalizer;

/**
 * The "barely hidden markov model" or "bicameral hidden markov model"
 *
 * @author tsmoon
 */
public class BHMMm1 extends BHMM {

    public BHMMm1(CommandLineOptions options) {
        super(options);
    }

    @Override
    protected void trainInnerIter(int itermax, String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initializeParametersRandom() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
