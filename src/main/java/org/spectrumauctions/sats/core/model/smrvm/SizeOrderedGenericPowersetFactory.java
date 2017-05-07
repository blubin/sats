/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowerset;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public class SizeOrderedGenericPowersetFactory {

    public static GenericPowerset<SMRVMGenericDefinition> getSizeOrderedGenericLang(boolean increasing, SMRVMBidder bidder) throws UnsupportedBiddingLanguageException {
        List<SMRVMGenericDefinition> bands = new ArrayList<>();
        for(SMRVMBand band : bidder.getWorld().getBands()){
            for(MRVMRegionsMap.Region region : bidder.getWorld().getRegionsMap().getRegions()){
                bands.add(new SMRVMGenericDefinition(band, region));
            }
        }
        if(increasing){
            return new Increasing(bands, bidder);
        }else{
            return new Decreasing(bands, bidder);
        }
    }
    
    private static final class Increasing extends GenericPowersetIncreasing<SMRVMGenericDefinition> {

        private SMRVMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Increasing(List<SMRVMGenericDefinition> genericDefinitions, SMRVMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public SMRVMBidder getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SMRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }
        
    }
    
    private static final class Decreasing extends GenericPowersetDecreasing<SMRVMGenericDefinition> {

        private SMRVMBidder bidder;

        /**
         * @param genericDefinitions
         * @throws UnsupportedBiddingLanguageException 
         */
        protected Decreasing(List<SMRVMGenericDefinition> genericDefinitions, SMRVMBidder bidder) throws UnsupportedBiddingLanguageException {
            super(genericDefinitions);
            this.bidder = bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.BiddingLanguage#getBidder()
         */
        @Override
        public Bidder<? extends Good> getBidder() {
            return bidder;
        }

        /* (non-Javadoc)
         * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.SizeOrderedPowerset.GenericPowerset#getGenericBidder()
         */
        @Override
        protected GenericValueBidder<SMRVMGenericDefinition> getGenericBidder() {
            return bidder;
        }
        
    }
}
