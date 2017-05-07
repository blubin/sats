/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValueBidder;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.*;
import org.spectrumauctions.sats.core.util.math.ContinuousPiecewiseLinearFunction;
import org.spectrumauctions.sats.core.util.random.JavaUtilRNGSupplier;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.*;

/**
 * @author Michael Weiss
 *
 */
public abstract class SMRVMBidder extends Bidder<SMRVMLicense> implements GenericValueBidder<SMRVMGenericDefinition> {
    

    /**
     * The function to calculate the qualitiy of a service. <br>
     * The input of this function is {@link SMRVMWorld#capShare(Region, Bundle)} - {@link beta},
     * i.e. the difference between the covered capacity share and the target capacity share.
     */
    public final ContinuousPiecewiseLinearFunction qualityFunction;
    
    private transient SMRVMWorld world;
    
    /**
     * A parameter defining an expected profit per served customer, if quality of service and regional discount are ignored.<br>
     * It can be understood as a relative bidder strength parameter.
     */
    private final BigDecimal alpha;
    
    /**
     * A parameter defining the target market share this bidder intends to cover. <br>
     * The bidders value for a bundle increases heavily as soon as the capacity share he has in a region gets close to 
     */
    private final BigDecimal beta;
    
    
    SMRVMBidder(long id, long populationId, SMRVMWorld world, SMRVMBidderSetup setup, UniformDistributionRNG rng){
        super(setup, populationId, id, world.getId());
        this.world = world;
        this.alpha = setup.drawAlpha(rng);
        this.beta = setup.drawBeta(rng);
        this.qualityFunction = defaultQualityFunction(beta);
    }
    
    private static ContinuousPiecewiseLinearFunction defaultQualityFunction(BigDecimal beta){
        Map<BigDecimal, BigDecimal> cornerPoints = new TreeMap<>();
        cornerPoints.put(beta.multiply(BigDecimal.valueOf(-1)), new BigDecimal(0));
        cornerPoints.put(BigDecimal.valueOf(-0.3).add(beta), BigDecimal.valueOf(0.27));
        cornerPoints.put(BigDecimal.valueOf(0.3).add(beta), BigDecimal.valueOf(0.73));
        cornerPoints.put(BigDecimal.ONE.add(beta), new BigDecimal(1));
        return new ContinuousPiecewiseLinearFunction(cornerPoints);
    }
   
    
  
    public BigDecimal quality(BigDecimal capacityShare){
        BigDecimal shareTargetDifference = capacityShare.subtract(beta);
        return qualityFunction.getY(shareTargetDifference);
    }
    
    /**
     * Calculates the omega factor 
     * @param r
     * @param quality
     * @return
     */
    public BigDecimal omegaFactor(Region r, BigDecimal quality){
        BigDecimal population = new BigDecimal(String.valueOf(r.getPopulation()));
        return quality.multiply(alpha).multiply(beta).multiply(population);
    }
        
    /**
     * Calculates the gamma factor, as explained in the model writeup. <br>
     * The gamma factor represents a bidder-specific discount of the the regional (omega) values.
     * @param r The region for which the discount is requested
     * @param bundle The complete bundle (not only containing the licenses of r).
     * @return
     */
    public abstract BigDecimal gammaFactor(Region r, Bundle<SMRVMLicense> bundle);
    
    /**
     * Calculates the gamma factors for all regions. For explanations of the gamma factors, see {@link #gammaFactor(Region, Bundle)}
     * @param bundle The bundle for which the discounts will be calculated.
     * @return
     */
    public abstract Map<Region, BigDecimal> gammaFactors(Bundle<SMRVMLicense> bundle);
    
    @Override
    public BigDecimal calculateValue(Bundle<SMRVMLicense> bundle){
        if(bundle.isEmpty()){
            return BigDecimal.ZERO;
        }
        //Pre filters the map such that for regional calculations, only licenses for the according region are in the passed (sub-)bundles.
        //This is for speedup of the calculation, but has no effect on the outcome of the value.
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<Region, Bundle<SMRVMLicense>> regionalBundles = SMRVMWorld.getLicensesPerRegion(bundle);
        //For speedup of calculation of global bidders, pre-compute gamma Factors for all requions in advance
        Map<Region, BigDecimal> gammaFactors = gammaFactors(bundle);
        //Calculate Regional Discounted Values and add them to total value
        for(Entry<Region, Bundle<SMRVMLicense>> regionalBundleEntry : regionalBundles.entrySet()){
            BigDecimal capShare = SMRVMWorld.capShare(regionalBundleEntry.getKey(), regionalBundleEntry.getValue());
            BigDecimal quality = quality(capShare);
            BigDecimal omegaFactor = omegaFactor(regionalBundleEntry.getKey(), quality);
            //Gamma Factor requires complete bundle (for global bidder to calculate #uncovered regions)
            BigDecimal gammaFactor = gammaFactors.get(regionalBundleEntry.getKey());
            BigDecimal discountedRegionalValue = omegaFactor.multiply(gammaFactor);
            totalValue = totalValue.add(discountedRegionalValue);
        }
        return totalValue;
    }
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericValueBidder#calculateValue(java.util.Map)
     */
    @Override
    public BigDecimal calculateValue(Map<SMRVMGenericDefinition, Integer> genericQuantities) {
        //TODO: Change this very naive approach to a faster one, where generics don't have to be transformed into bundles
        Bundle<SMRVMLicense> bundle = new Bundle<>();
        Map<SMRVMGenericDefinition, Integer> addedQuantities = new HashMap<>();
        for(Region region : getWorld().getRegionsMap().getRegions()){
            for(SMRVMBand band : getWorld().getBands()){
                addedQuantities.put(new SMRVMGenericDefinition(band, region), 0);
            }
        }
        for(SMRVMLicense license : getWorld().getLicenses()){
            SMRVMGenericDefinition def = new SMRVMGenericDefinition(license.getBand(), license.getRegion());
            Integer requiredQuantity = genericQuantities.get(def);
            Integer addedQuantity = addedQuantities.get(def);
            if(requiredQuantity != null && requiredQuantity > addedQuantity){
                bundle.add(license);
                addedQuantities.put(def, addedQuantity+1);
            }
        }
        return calculateValue(bundle);
    }   
    
   
    @Override
    public SMRVMWorld getWorld(){
        return this.world;
    }

    private void setWorld(SMRVMWorld world) {
        this.world = world;
    }
    
    
    public BigDecimal getAlpha() {
        return alpha;
    }

    public BigDecimal getBeta() {
        return beta;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.Bidder#refreshReference(ch.uzh.ifi.ce.mweiss.specval.model.World)
     */
    @Override
    public void refreshReference(World world) {
        if(world instanceof SMRVMWorld){
            setWorld((SMRVMWorld) world);
        }else{
            throw new IncompatibleWorldException("Wrong world class");
        }

    }

    
    
    
    @Override
    public <T extends BiddingLanguage> T getValueFunction(Class<T> clazz, long seed)
            throws UnsupportedBiddingLanguageException {
        if (clazz.isAssignableFrom(SizeBasedUniqueRandomXOR.class)) {
            return clazz.cast(
                    new SizeBasedUniqueRandomXOR<>(world.getLicenses(), new JavaUtilRNGSupplier(seed), this));
        } else if (clazz.isAssignableFrom(IncreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new IncreasingSizeOrderedXOR<SMRVMLicense>(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(DecreasingSizeOrderedXOR.class)) {
            return clazz.cast(
                    new DecreasingSizeOrderedXOR<SMRVMLicense>(world.getLicenses(), this));
        } else if (clazz.isAssignableFrom(GenericSizeIncreasing.class)){
            return clazz.cast(
                    SizeOrderedGenericFactory.getSizeOrderedGenericLang(true, this));
        } else if (clazz.isAssignableFrom(GenericSizeDecreasing.class)){
            return clazz.cast(
                    SizeOrderedGenericFactory.getSizeOrderedGenericLang(false, this));
        } else if (clazz.isAssignableFrom(GenericPowersetIncreasing.class)){
            return clazz.cast(
                    SizeOrderedGenericPowersetFactory.getSizeOrderedGenericLang(true, this));
        } else if (clazz.isAssignableFrom(GenericPowersetDecreasing.class)){
            return clazz.cast(
                    SizeOrderedGenericPowersetFactory.getSizeOrderedGenericLang(false, this));
        } else {
            throw new UnsupportedBiddingLanguageException();
        } 
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
        result = prime * result + ((beta == null) ? 0 : beta.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SMRVMBidder other = (SMRVMBidder) obj;
        if (alpha == null) {
            if (other.alpha != null)
                return false;
        } else if (!alpha.equals(other.alpha))
            return false;
        if (beta == null) {
            if (other.beta != null)
                return false;
        } else if (!beta.equals(other.beta))
            return false;
        return true;
    }
    
    
}
