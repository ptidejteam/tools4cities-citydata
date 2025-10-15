from metamenth.datatypes.interfaces.abstract_measure import AbstractMeasure
from .measure import Measure

class DataMeasure(AbstractMeasure):
    
    def __init__(self, measure: Measure):
        super().__init__(measure, None)
        self.setValue(measure.getMinimum())
        
    def setValue(self, value: float):
        self._value = value

    def getValue(self) -> float:
        return self._value
    
    def toString(self):
        return (
            f"DataMeasure("
            f"Value: {self.getValue()}"
        )
        
    class Java:
        implements = ['ca.concordia.ngci.tools4cities.metamenth.interfaces.datatypes.IDataMeasure']        