/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeOrdered;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.*;


/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericFactory {

    static BandComparator comparator = new BandComparator();
    
    public static GenericSizeOrdered<SMRVMGenericDefinition> getSizeOrderedGenericLang(boolean increasing, SMRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<SMRVMGenericDefinition> bands = new ArrayList<>();
        for(SMRVMBand band : bidder.getWorld().getBands()){
            for(Region region : bidder.getWorld().getRegionsMap().getRegions()){
                bands.add(new SMRVMGenericDefinition(band, region));
            }
        }
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    
    private static final class Increasing extends GenericSizeIncreasing<SMRVMGenericDefinition> {

        
        private final SMRVMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Increasing(Collection<SMRVMGenericDefinition> allPossibleGenericDefintions, SMRVMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<SMRVMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SMRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SMRVMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }

    private static final class Decreasing extends GenericSizeDecreasing<SMRVMGenericDefinition> {

        
        private final SMRVMBidder bidder;

        /**
         * @param allPossibleGenericDefintions
         * @throws UnsupportedBiddingLanguageException
         */
        protected Decreasing(Collection<SMRVMGenericDefinition> allPossibleGenericDefintions, SMRVMBidder bidder)
                throws UnsupportedBiddingLanguageException {
            super(allPossibleGenericDefintions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<SMRVMLicense> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SMRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrdered.GenericSizeOrdered#getDefComparator()
         */
        @Override
        protected Comparator<SMRVMGenericDefinition> getDefComparator() {
            return comparator;
        }
    }

    
    private static class BandComparator implements Comparator<SMRVMGenericDefinition>{

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(SMRVMGenericDefinition o1, SMRVMGenericDefinition o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
    

}
