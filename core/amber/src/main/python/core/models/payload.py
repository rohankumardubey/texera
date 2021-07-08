from dataclasses import dataclass
from typing import List

from core.models.tuple import ITuple, Tuple


@dataclass
class DataPayload:
    pass


@dataclass
class DataFrame(DataPayload):
    frame: list[Tuple]


@dataclass
class EndOfUpstream(DataPayload):
    pass
